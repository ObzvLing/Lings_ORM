package javaORM;
import java.util.List;

public class SKStore {
    private StringBuilder sql;
    private List<String> keyList;

    public SKStore(StringBuilder sql, List<String> keyList){
        this.keyList = keyList;
        this.sql = sql;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public String getSql() {
        return sql.toString();
    }
}
