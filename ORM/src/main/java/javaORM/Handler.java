package javaORM;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 为SqlSession类解析带有#{key}内容的特殊sql
 */
public class Handler {

    SKStore parseSQL(String sql){
        StringBuilder newSql = new StringBuilder();
        List<String> keyList = new ArrayList<>();
        while (true) {
            int left = sql.indexOf("#{");
            int right = sql.indexOf("}");
            if (left != -1 && right != -1 && left < right) {
                newSql.append(sql.substring(0, left));
                newSql.append("?");
                keyList.add(sql.substring(left + 2, right));
            } else {
                newSql.append(sql);
                break;
            }
            sql = sql.substring(right+1);
        }
        return new SKStore(newSql, keyList);
    }

    void compareParameter(PreparedStatement pstat, Object obj, List<String> keyList) throws SQLException {
        Class clazz = obj.getClass();
        if(clazz==int.class || clazz==Integer.class){
            pstat.setInt(1,(Integer) obj);
        }else if(clazz==float.class || clazz==Float.class){
            pstat.setFloat(1,(Float)obj);
        }else if(clazz==double.class || clazz==Double.class){
            pstat.setDouble(1,(Double)obj);
        }else if(clazz==String.class){
            pstat.setString(1,(String) obj);
        }else if(obj instanceof Map){
            compareMap(pstat,obj,keyList);
        }else{
            compareDomain(pstat,obj,keyList);
        }
    }

    private void compareMap(PreparedStatement pstat, Object obj, List<String> keyList) throws SQLException {
        Map map = (Map)obj;
        for(int i = 0; i<keyList.size(); i++){
            pstat.setObject(i+1,map.get(keyList.get(i)));
        }
    }
    private void compareDomain(PreparedStatement pstat, Object obj, List<String> keyList) throws SQLException {
        try{
            Class clazz = obj.getClass();
            for (int i = 0; i< keyList.size(); i++){
                String key = keyList.get(i);
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);
                Object value = field.get(obj);
                System.out.println(value);
                pstat.setObject(i+1,value);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Object handleResult(ResultSet rs, Class classType) throws SQLException{
        Object result = null;
        if(classType==int.class || classType==Integer.class){
            result = rs.getInt(1);
        }else if(classType==float.class || classType==Float.class){
            result = rs.getFloat(1);
        }else if(classType==double.class || classType==Double.class){
            result = rs.getDouble(1);
        }else if(classType==String.class){
            result = rs.getString(1);
        }else if(classType==Map.class){
            result = getMap(rs);
        }else {
            result = getDomain(rs,classType);
        }
        return result;
    }

    private Map getMap(ResultSet rs) throws SQLException{
        Map result = new HashMap<String,Object>();
        ResultSetMetaData rsmd = rs.getMetaData();
        for(int i = 1; i<=rsmd.getColumnCount(); i++){
            String key = rsmd.getColumnName(i);
            Object value = rs.getObject(key);
            result.put(key,value);
        }
        return result;
    }
    /*
    外键不存在当前domain对象中，赋值时没有对应属性。可以通过多次查找解决，也可以递归解决
     */
    private Object getDomain(ResultSet rs, Class resultType) throws SQLException{
        Object result = null;
        try{
            result = resultType.newInstance();
            ResultSetMetaData rsmd = rs.getMetaData();
            for(int i = 1; i<= rsmd.getColumnCount(); i++){
                String columnName = rsmd.getColumnName(i);
                Field field = resultType.getDeclaredField(columnName);
                field.setAccessible(true);
                field.set(result,rs.getObject(columnName));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
