package pool;

import java.sql.Connection;
import java.util.ArrayList;

/**
 *
 */
public class ConnectionPool {
    private ArrayList<Connection> myConnList = new ArrayList<>();

    {//创建连接池对象时给它赋值
        int minCount = ConfigurationReader.getIntValue("minCount");
        for(int i = 1; i<=minCount; i++){
            myConnList.add(new MyConnection());
        }
    }

    //从连接池中获取一个可用连接
    private Connection getMC(){
        Connection myConnection = null;
        for(int i = 0; i<myConnList.size(); i++){
            MyConnection mc = (MyConnection)myConnList.get(i);
            synchronized (this) {
                if (!mc.isStatus()) {
                    mc.setStatus(true);
                    myConnection = mc;
                    break;
                }
            }
        }
        return myConnection;
    }

    //排队等待机制
    public Connection getMyConnection(){
        Connection result = this.getMC();
        int count = 1;
        int waitTime = ConfigurationReader.getIntValue("waitTime");
        while (result == null && count<waitTime*10){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = this.getMC();
            count++;
        }
        if(result == null){
            throw new ConnectionPoolSystemBusyException("系统繁忙，稍后再试");
        }
        return result;
    }
}
