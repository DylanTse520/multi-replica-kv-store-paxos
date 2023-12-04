// Server.java: the server interface

import java.net.SocketTimeoutException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

    // The method to ask server to operate on its store
    StoreOperationResult operateStore(StoreOperation operation) throws RemoteException,
            SocketTimeoutException;

    // The method to ask server to prepare for an operation in Paxos
    boolean prepare(int proposalId, Integer initPort) throws RemoteException;

    // The method to ask server to accept an operation in Paxos
    boolean accept(int proposalId, StoreOperation operation, Integer initPort) throws RemoteException;

    // The method to ask server to commit an operation in Paxos
    StoreOperationResult commit(StoreOperation operation, Integer initPort) throws RemoteException;

}
