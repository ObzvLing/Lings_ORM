package javaORM;
import javaORM.annotation.*;
import pool.ConnectionPool;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class SqlSession {
    private Handler handler = new Handler();
    private ConnectionPool pool = new ConnectionPool();
    /*
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/test?useSSL=true&serverTimezone=Asia/Shanghai";
    private String user = "root";
    private String password = "SL51ef5c145e0c";
        */
    public <T>T getMapper(Class daoClass){
        //获取一个代理对象，需要三个条件
        //1.类加载器（ClassLoader）
        ClassLoader loader = daoClass.getClassLoader();
        //2.Class类型的数组（Class[]），通常只存放一个参数
        Class[] interfaces = new Class[]{daoClass};
        //3.InvocationHandler接口
        InvocationHandler invocationHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                /*
                invoke方法是指代理对象具体做事的方式
                proxy代理对象就是方法的执行者
                method代理对象，代替真实Dao执行的方法
                args代理对象，代替Dao执行方法的参数
                 */
                //获取注解及其名称和内部的sql
                Annotation a = method.getAnnotations()[0];
                Class clazz = a.annotationType();
                Method valueMethod = clazz.getDeclaredMethod("value");
                String sql = (String) valueMethod.invoke(a);
                //根据注解名调用SqlSession中的方法
                Object param = args==null?null:args[0];
                if(clazz == Insert.class || clazz == Delete.class || clazz == Update.class){
                    SqlSession.this.update(sql,param);
                }else if(clazz == Query.class){
                    //此时不能确定调用方法查询的是单条还是多条，通过查询方法的返回值来判断
                    Class returnType = method.getReturnType();//获取返回值类型List
                    if(returnType == ArrayList.class){
                        Type type = method.getGenericReturnType();//获取返回值的全类型java.util.List<domain.XXXX>
                        System.out.println(type);
                        ParameterizedTypeImpl realType = (ParameterizedTypeImpl)type;//还原为真实类
                        Type[] realTypeActualTypeArguments = realType.getActualTypeArguments();//操作返回值类型，获取泛型类
                        Type patternType = realTypeActualTypeArguments[0];
                        Class resultType = (Class)patternType;//还原为方法参数所需类型
                        return SqlSession.this.query(sql,param,resultType);
                    }else {
                        return SqlSession.this.selectOne(sql,param,returnType);
                    }
                }
                return null;
            }
        };
       return (T)Proxy.newProxyInstance(loader,interfaces,invocationHandler);
    }

    public void update(String sql, Object obj){
        SKStore store = handler.parseSQL(sql);
        try{
            Connection conn = pool.getMyConnection();
            PreparedStatement pstat = conn.prepareStatement(store.getSql());
            if(obj!=null) {
                handler.compareParameter(pstat, obj, store.getKeyList());
            }
            pstat.executeUpdate();
            pstat.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public <T> List<T> query(String sql, Object obj, Class classType){
        SKStore store = handler.parseSQL(sql);
        List<T> list = new ArrayList<>();
        try{
            Connection conn = pool.getMyConnection();
            PreparedStatement pstat = conn.prepareStatement(store.getSql());
            if(obj!=null) {
                handler.compareParameter(pstat, obj, store.getKeyList());
            }
            ResultSet rs = pstat.executeQuery();
            while (rs.next()) {
                list.add((T)handler.handleResult(rs, classType));
            }
            rs.close();
            pstat.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public <T>T selectOne(String sql, Object obj, Class classType){
        return (T)this.query(sql,obj,classType).get(0);
    }
}