package net.openright.infrastructure.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openright.infrastructure.util.ExceptionUtil;

public class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    public interface RowMapper<T> {
        @Nullable
        T run(Row row) throws SQLException;
    }

    public static class Row {

        private final ResultSet rs;
        private final Map<String, Integer> columnMap = new HashMap<>();

        public Row(ResultSet rs) throws SQLException {
            this.rs = rs;
            for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                String tableName = rs.getMetaData().getTableName(i);
                String columnName = rs.getMetaData().getColumnName(i);

                this.columnMap.put(tableName + "." + columnName, i);
            }
        }

        @Nullable
        public String getString(String string) throws SQLException {
            return rs.getString(string);
        }

        public int getInt(String columnName) throws SQLException {
            return rs.getInt(columnName);
        }

        @Nullable
        public Instant getInstant(String columnName) throws SQLException {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toInstant() : null;
        }

        public Long getLong(String columnName) throws SQLException {
            return rs.getLong(columnName);
        }

        public long getLong(String tableName, String columnName) throws SQLException {
            return rs.getLong(getColumnIndex(tableName, columnName));
        }

        @Nullable
        public String getString(String tableName, String columnName) throws SQLException {
            return rs.getString(getColumnIndex(tableName, columnName));
        }

        public boolean getBoolean(String tableName, String columnName) throws SQLException {
            return rs.getBoolean(getColumnIndex(tableName, columnName));
        }

        public double getDouble(String tableName, String columnName) throws SQLException {
            return rs.getDouble(getColumnIndex(tableName, columnName));
        }

        private int getColumnIndex(String tableName, String columnName) {
            return columnMap.get(tableName + "." + columnName);
        }

    }

    private final DataSource dataSource;
    private final static ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Database(String name) {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup(name);
        } catch (NamingException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    /**
     * Insert an object into the database. Used for create operations.
     *
     * @param query
     *            in SQL stated as a prepared statement.
     * @param parameters
     *            values for the prepared statement.
     * @return the database field id from the inserted element.
     */
    public long insert(String query, Collection<Object> parameterList) {
        return executeDbOperation(query, parameterList, stmt -> {
            stmt.execute();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }, Statement.RETURN_GENERATED_KEYS);
    }

    /**
     * Retrieves a list of results from the database and maps it to an object.
     *
     * @param query
     *            in SQL stated as a prepared statement.
     * @param mapper
     *            definition for mapping fields to the returned objects in the
     *            list.
     * @param parameters
     *            for the prepared statement.
     * @return database result mapped to list of classes.
     */
    @Nonnull
    public <T> List<T> queryForList(String query, RowMapper<T> mapper, Object... parameters) {
        return queryForList(query, Arrays.asList(parameters), mapper);
    }

    @Nonnull
    public <T> List<T> queryForList(String query, List<Object> parameterList, RowMapper<T> mapper) {
        return executeDbOperation(query, parameterList, stmt -> {
            try (ResultSet rs = stmt.executeQuery()) {
                Row row = new Row(rs);
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.run(row));
                }
                return result;
            }
        }, Statement.NO_GENERATED_KEYS);
    }

    /**
     * Retrieves a single result from the database and maps it to an objec.
     *
     * @param query
     *            in SQL stated as a prepared statement.
     * @param parameters
     *            for the prepared statement.
     * @param mapper
     *            definition for mapping fields to the returned object.
     * @return database result mapped to class.
     */
    public <T> Optional<T> queryForSingle(String query, Collection<Object> parameters, RowMapper<T> mapper) {
        return executeDbOperation(query, parameters, stmt -> {
            try (ResultSet rs = stmt.executeQuery()) {
                return mapSingleRow(rs, mapper);
            }
        }, Statement.NO_GENERATED_KEYS);
    }

    @Nonnull
    public <T> Optional<T> queryForSingle(String query, String parameter, RowMapper<T> mapper) {
        return queryForSingle(query, Collections.singletonList(parameter), mapper);
    }

    @Nonnull
    public <T> Optional<T> queryForSingle(String query, long parameter, RowMapper<T> mapper) {
        return queryForSingle(query, Collections.singletonList(parameter), mapper);
    }

    /**
     * Update or delete operation sent to the database.
     *
     * @param query
     *            in SQL stated as a prepared statement.
     * @param parameters
     *            for the prepared statement.
     */
    public void executeOperation(@Nonnull String query, @Nonnull Object... parameters) {
        executeDbOperation(query, Arrays.asList(parameters));
    }

    public Integer executeDbOperation(String query, Collection<Object> collection) {
        return executeDbOperation(query, collection, PreparedStatement::executeUpdate, Statement.NO_GENERATED_KEYS);
    }

    /**
     * Create a transaction for multiple database operations like
     * {@link #insert(String, Object...) insert},
     * {@link #queryForList(String, RowMapper, Object...) queryForList},
     * {@link #queryForSingle(String, RowMapper, Object...) queryForSingle} or
     * {@link #executeOperation(String, Object...) executeOperation}
     *
     * @param operation
     *            is a functional interface to allow transaction to run in a
     *            thread.
     */
    public void doInTransaction(@Nonnull Runnable operation) {
        try (Connection connection = dataSource.getConnection()) {
            threadConnection.set(connection);
            try {
                operation.run();
            } finally {
                threadConnection.set(null);
            }
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    private interface ConnectionCallback<T> {
        T run(Connection conn) throws SQLException;
    }

    private <T> T doWithConnection(ConnectionCallback<T> object) throws SQLException {
        if (threadConnection.get() != null) {
            return object.run(threadConnection.get());
        }

        try (Connection conn = dataSource.getConnection()) {
            return object.run(conn);
        }
    }

    private interface StatementCallback<T> {
        T run(PreparedStatement stmt) throws SQLException;
    }

    private <T> T executeDbOperation(String query, Collection<Object> parameters,
            StatementCallback<T> statementCallback, int generateKeys) {
        try {
            return doWithConnection(conn -> {
                log.info("Executing: {} with params {}", query, parameters);
                try (PreparedStatement prepareStatement = conn.prepareStatement(query, generateKeys)) {
                    int index = 1;
                    for (Object object : parameters) {
                        setParameter(prepareStatement, index, object);
                        index += 1;
                    }

                    return statementCallback.run(prepareStatement);
                }
            });
        } catch (SQLException e) {
            log.info("Error while executing {} {}", query, e);
            throw new RuntimeException(e.getMessage() + " on " + query);
        }
    }

    public void setParameter(PreparedStatement stmt, int index, Object object) throws SQLException {
        if (object instanceof Instant) {
            stmt.setTimestamp(index, new Timestamp(((Instant)object).toEpochMilli()));
        } else {
            stmt.setObject(index, object);
        }
    }

    private RuntimeException handleException(SQLException e) {
        return ExceptionUtil.soften(e);
    }

    private <T> Optional<T> mapSingleRow(ResultSet rs, RowMapper<T> mapper) throws SQLException {
        if (!rs.next()) {
            return Optional.empty();
        }
        T result = mapper.run(new Row(rs));
        if (rs.next()) {
            throw new RuntimeException("Duplicate");
        }
        return Optional.of(result);
    }
}
