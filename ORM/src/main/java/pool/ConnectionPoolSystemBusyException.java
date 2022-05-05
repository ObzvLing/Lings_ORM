package pool;

public class ConnectionPoolSystemBusyException extends RuntimeException{
    public ConnectionPoolSystemBusyException(){

    }
    public ConnectionPoolSystemBusyException(String msg){
        super(msg);
    }
}
