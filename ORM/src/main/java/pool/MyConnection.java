package pool;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 静态代理
 * MyConnection和JDBC4Connection类都是Connection接口的实现
 * MyConnection类的属性中存放了JDBC4Connection类的对象，就能使用这个对象完成对应的方法
 * 看起来像是代理对象实现的，实际上借用了原来的对象
 */
public class MyConnection extends AbstractConnection {
    private Connection conn;
    private boolean status = false;//false空闲的，true被占用

    private static String driver = ConfigurationReader.getStringValue("driver");
    private static String url = ConfigurationReader.getStringValue("url");
    private static String user = ConfigurationReader.getStringValue("user");
    private static String password = ConfigurationReader.getStringValue("password");

    static {//让加载驱动这件事只做一次
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    {//创建对象时给conn赋值
        try {
            conn = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return conn;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return null;
    }
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement pstat = this.conn.prepareStatement(sql);
        return pstat;
    }
    @Override
    public void close() throws SQLException {
        this.status = false;
    }
}