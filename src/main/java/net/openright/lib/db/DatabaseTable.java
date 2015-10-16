package net.openright.lib.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Tainted;
import javax.annotation.Untainted;

import net.openright.infrastructure.db.Database;

public class DatabaseTable {

    private Database database;
    private String tableName;

    public interface Inserter {

        void values(Map<String, Object> row);

    }

    public class DatabaseConstrainedStatement {

        public List<String> whereClauses = new ArrayList<>();
        public List<Object> parameters = new ArrayList<>();

        public <T> List<T> list(Database.RowMapper<T> mapper) {
            return database.queryForList(createQuery(), parameters, mapper);
        }

        private String createQuery() {
            return "select * from " + tableName
                    + " where " + String.join(" and ", whereClauses);
        }

    }

    public DatabaseTable(Database database, String tableName) {
        this.database = database;
        this.tableName = tableName;
    }

    public DatabaseConstrainedStatement where(@Untainted String column, @Tainted Object value) {
        DatabaseConstrainedStatement statement = new DatabaseConstrainedStatement();
        statement.whereClauses.add(column + " = ?");
        statement.parameters.add(value);
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
