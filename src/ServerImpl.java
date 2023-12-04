// ServerImpl.java: the class implementing the server interface

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

// Extending UnicastRemoteObject to allow RMI registry
public class ServerImpl extends UnicastRemoteObject implements Server {

    // The proposer in Paxos
    Proposer proposer;

    // The acceptor in Paxos
    Acceptor acceptor;

    // The learner in Paxos
    Learner learner;

    // The key value store
    HashMap<String, String> keyValueStore;

    // The port number for this server
    Integer port;

    // The port numbers of other servers
    ArrayList<Integer> allPorts;

    // Write explicit constructor to declare the RemoteException exception
    public ServerImpl(Integer port, ArrayList<Integer> allPorts) throws RemoteException {
        super();
        this.keyValueStore = new HashMap<>();
        this.port = port;
        this.allPorts = allPorts;
        this.proposer = new Proposer(this.port, this.allPorts);
        this.acceptor = new Acceptor(this.keyValueStore, this.port, this.allPorts);
        this.learner = new Learner(this.keyValueStore, this.port);
    }

    @Override
    public StoreOperationResult operateStore(StoreOperation operation) throws RemoteException {
        // Ask proposer to propose an operation
        return proposer.propose(operation);
    }

    @Override
    public boolean prepare(int proposalId, Integer initPort) throws RemoteException {
        // Ask acceptor to prepare for an operation
        return acceptor.prepare(proposalId, initPort);
    }

    @Override
    public boolean accept(int proposalId, StoreOperation operation, Integer initPort) throws RemoteException {
        // Ask acceptor to accept an operation
        return acceptor.accept(proposalId, operation, initPort);
    }

    @Override
    public StoreOperationResult commit(StoreOperation operation, Integer initPort) throws RemoteException {
        // Ask learner to commit an operation
        return learner.commit(operation, initPort);
    }

}
