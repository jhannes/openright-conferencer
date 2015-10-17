package net.openright.lib.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Tainted;
import javax.annotation.Untainted;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.db.Database.RowMapper;

public class DatabaseTable {


    private Database database;
    private String tableName;

    public interface Inserter {

        void values(Map<String, Object> row);

    }

    public class ConstrainedStatement {

        List<String> whereClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        List<String> innerJoins = new ArrayList<>();

        public <T> Optional<T> single(RowMapper<T> mapper) {
            return database.queryForSingle(createSelectQuery(), parameters, mapper);
        }

        protected String createSelectQuery() {
            StringBuilder query = new StringBuilder();
            query.append("select * from ");
            query.append(tableName);
            query.append(" ");
            query.append(String.join(" ", innerJoins));
            query.append(" where ");
            query.append(String.join(" and ", whereClauses));
            return query.toString();
        }

        public SortedConstrainedStatement orderBy(String string) {
            SortedConstrainedStatement result = cloneStatement(new SortedConstrainedStatement());
            result.sorts.add(string);
            return result;
        }


        public ConstrainedStatement where(@Untainted String column, @Tainted Object value) {
            ConstrainedStatement result = cloneStatement(new ConstrainedStatement());
            result.whereClauses.add(column + " = ?");
            result.parameters.add(value);
            return result;
        }

        public ConstrainedStatement whereNotIn(String column, Set<? extends Object> values) {
            ConstrainedStatement result = cloneStatement(new ConstrainedStatement());
            result.whereClauses.add(
                    column + " not in (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")");
            result.parameters.addAll(values);
            return result;
        }

        private <T extends ConstrainedStatement> T cloneStatement(T statement) {
            statement.whereClauses.addAll(whereClauses);
            statement.innerJoins.addAll(innerJoins);
            statement.parameters.addAll(parameters);
            return statement;
        }

        public void delete() {
            database.executeDbOperation(createDeleteQuery(), parameters);
        }

        private String createDeleteQuery() {
            return "delete from " + tableName + " where " + String.join(" AND ", whereClauses);
        }

        public void update(Inserter updater) {
            Map<String, Object> row = new LinkedHashMap<>();
            updater.values(row);
            List<Object> parameters = new ArrayList<>(row.values());
            parameters.addAll(this.parameters);
            database.executeDbOperation(createUpdateQuery(row.keySet()), parameters);
        }

        private String createUpdateQuery(Set<String> keySet) {
            StringBuilder query = new StringBuilder();
            query.append("update ");
            query.append(tableName);
            query.append(" set ");
            query.append(keySet.stream().map(s -> s + " = ?").collect(Collectors.joining(", ")));
            query.append(" where ");
            query.append(String.join(" and ", whereClauses));
            return query.toString();
        }
    }

    public class SortedConstrainedStatement extends ConstrainedStatement {

        public List<String> sorts = new ArrayList<>();

        public <T> List<T> list(Database.RowMapper<T> mapper) {
            return database.queryForList(createSelectQuery(), parameters, mapper);
        }

        @Override
        protected String createSelectQuery() {
            return super.createSelectQuery() + " ORDER BY " + String.join(", ", sorts);
        }
    }

    public DatabaseTable(Database database, String tableName) {
        this.database = database;
        this.tableName = tableName;
    }

    public CharSequence repeat(int n, String s) {
        return new String(new char[n]).replace("\0", s);
    }

    @CheckReturnValue
    public ConstrainedStatement where(@Untainted String column, @Tainted Object value) {
        return new ConstrainedStatement().where(column, value);
    }

    @CheckReturnValue
    public ConstrainedStatement innerJoin(DatabaseTable otherTable, String otherTableId, String thisTableId) {
        ConstrainedStatement statement = new ConstrainedStatement();
        statement.innerJoins.add(
                "INNER JOIN " +  otherTable.tableName
                + " ON " + otherTable.tableName + "." + otherTableId
                + " = " + tableName + "." + thisTableId);
        return statement;
    }


    public long insert(Inserter inserter) {
        Map<String, Object> row = new LinkedHashMap<>();
        inserter.values(row);
        return database.insert(createInsertQuery(row.keySet()), row.values());
    }

    private String createInsertQuery(Set<String> keySet) {
        return "insert into " + tableName + " ("
                + String.join(",", keySet)
                + ") values ("
                + keySet.stream().map(s -> "?").collect(Collectors.joining(","))
                + ")";
    }


}
