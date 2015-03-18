package net.openright.infrastructure.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

public class PgsqlDatabase {

	public interface ConnectionCallback<T> {

		T run(Connection conn);

	}

	public interface StatementCallback<T> {

		T run(PreparedStatement stmt) throws SQLException;

	}

	public interface ResultSetMapper<T> {

		T run(ResultSet rs) throws SQLException;

	}

	public interface Inserter {

		void values(Map<String, Object> row);

	}

	public class QualifiedDatabaseTable {
		private LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
		private String tableName;


		public QualifiedDatabaseTable(String tableName, String field, Object value) {
			this.tableName = tableName;
			parameters.put(field, value);
		}


		public <T> T single(ResultSetMapper<T> mapper) {
			return executeQuery(getQuery(), parameters.values(), rs -> {
				if (!rs.next()) {
					throw new RuntimeException("Not found");
				}
				T result = mapper.run(rs);
				if (rs.next()) {
					throw new RuntimeException("Duplicat");
				}
				return result;
			});
		}

		public <T> List<T> list(ResultSetMapper<T> mapper) {
			return executeListQuery(getQuery(), parameters.values(), mapper);
		}

		private String getQuery() {
			String whereClause = parameters.keySet().stream()
					.map(s -> s + " = ?")
					.collect(Collectors.joining(" and "));
			return "select * from " + tableName + " where " + whereClause;
		}
	}

	public class DatabaseTable {

		private String tableName;

		public DatabaseTable(String tableName) {
			this.tableName = tableName;
		}

		public int insertValues(Inserter inserter) {
			HashMap<String, Object> row = new HashMap<String, Object>();
			inserter.values(row);
			return executeOperation(insertQuery(row.keySet()), row.values(), stmt -> {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				return rs.getInt("id");
			});
		}

		private String insertQuery(Collection<String> columnNames) {
			return "insert into " + tableName + " ("
				+ String.join(", ", columnNames) + ") values ("
				+ String.join(", ", repeat("?", columnNames.size()))
				+ ") returning id";
		}

		public <T> List<T> list(ResultSetMapper<T> mapper) {
			return executeListQuery(getQuery(), new ArrayList<Object>(), mapper);
		}

		private String getQuery() {
			return "select * from " + tableName;
		}

		public QualifiedDatabaseTable where(String field, Object value) {
			return new QualifiedDatabaseTable(tableName, field, value);
		}

	}

	private final DataSource dataSource;
	private final static ThreadLocal<Connection> threadConnection = new ThreadLocal<>();


	public PgsqlDatabase(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private List<String> repeat(String string, int count) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			result.add(string);
		}
		return result;
	}

	public <T> T executeOperation(String query, Collection<Object> parameters, StatementCallback<T> statementCallback) {
		return doWithConnection(conn -> {
			try (PreparedStatement prepareStatement = conn.prepareStatement(query)) {
				int index = 1;
				for (Object object : parameters) {
					prepareStatement.setObject(index++, object);
				}

				return statementCallback.run(prepareStatement);
			} catch (SQLException e) {
				throw convertException(e);
			}
		});

	}

	public <T> T executeQuery(String query, ResultSetMapper<T> mapper) {
		return executeQuery(query, new ArrayList<Object>(), mapper);
	}

	public <T> List<T> executeListQuery(String query, Collection<Object> parameters, ResultSetMapper<T> mapper) {
		return executeQuery(query, parameters, rs -> {
			List<T> result = new ArrayList<T>();
			while (rs.next()) {
				result.add(mapper.run(rs));
			}
			return result;
		});
	}

	public <T> T executeQuery(String query, Collection<Object> parameters, ResultSetMapper<T> mapper) {
		return executeOperation(query, parameters, stmt -> {
			try (ResultSet rs = stmt.executeQuery()) {
				return mapper.run(rs);
			}
		});
	}

	public void doInTransaction(Runnable operation) {
		try (Connection connection = dataSource.getConnection()) {
			threadConnection.set(connection);
			try {
				operation.run();
			} finally {
				threadConnection.set(null);
			}
		} catch (SQLException e) {
			throw convertException(e);
		}

	}

	private <T> T doWithConnection(ConnectionCallback<T> object) {
		if (threadConnection.get() != null) {
			return object.run(threadConnection.get());
		}

		try (Connection conn = dataSource.getConnection()) {
			return object.run(conn);
		} catch (SQLException e) {
			throw convertException(e);
		}
	}

	public ResultSet executeQuery(String query) {
		return null;
	}

	public DatabaseTable table(String tableName) {
		return new DatabaseTable(tableName);
	}

	public static RuntimeException convertException(SQLException e) {
		return new RuntimeException(e);
	}

}