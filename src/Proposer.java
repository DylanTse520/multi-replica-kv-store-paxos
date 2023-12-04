// Proposer.java: the class implementing the proposer

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Proposer implements Runnable {

    // The proposal id this proposer maintains
    private static int proposalId;

    // The port of the server having this proposer
    Integer port;

    // The port numbers of all servers
    ArrayList<Integer> allPorts;

    public Proposer(Integer port, ArrayList<Integer> allPorts) {
        this.port = port;
        this.allPorts = allPorts;
        proposalId = 0;
    }

    // Propose an operation and see the result
    public synchronized StoreOperationResult propose(StoreOperation operation) {
        Helper.log(this.port + " received request for " + operation + ".");
        // Creating the result object
        StoreOperationResult result = new StoreOperationResult(false, "");

        // Increase proposal id
        proposalId++;
        // Count response
        int count = 0;
        // Count attempt
        int attempt = 0;

        // Prepare phase
        while (attempt < 3) {
            attempt++;

            Helper.log(this.port + " is preparing other servers for " + operation + ".");
            // For each port, ask to prepare for operation
            for (Integer port : allPorts) {
                try {
                    // Get the registry
                    Registry registry = LocateRegistry.getRegistry("localhost", port);
                    // Getting the Sort class object
                    Server server = (Server) registry.lookup("PaxosServer");
                    // Prepare other server and count response
                    if (server.prepare(proposalId, this.port)) {
                        count++;
                    }
                } catch (NotBoundException | RemoteException e) {
                    Helper.log(this.port + " failed to prepare " + port + " for " + operation + ".");
                }
            }
            Helper.log(this.port + " finished preparing other servers for " + operation + ".");

            if (count > allPorts.size() / 2) {
                Helper.log(this.port + " succeed preparing other servers for " + operation + ".");
                break;
            }
        }
        if (attempt == 3 && count <= allPorts.size() / 2) {
            Helper.log(this.port + " is not getting preparation consensus.");
            return new StoreOperationResult(false, "Preparation consensus could not be reached with " + count + " server prepared.");
        }
        System.out.println(attempt + "--------------" + count);

        // New counting
        count = 0;
        attempt = 0;

        // Accept phase
        while (attempt < 3) {
            attempt++;

            Helper.log(this.port + " is asking other servers to accept " + operation + ".");
            // For each port, ask to accept operation
            for (Integer port : allPorts) {
                try {
                    // Get the registry
                    Registry registry = LocateRegistry.getRegistry("localhost", port);
                    // Getting the Sort class object
                    Server server = (Server) registry.lookup("PaxosServer");
                    // Ask other server to accept operation and count response
                    if (server.accept(proposalId, operation, this.port)) {
                        count++;
                    }
                } catch (NotBoundException | RemoteException e) {
                    Helper.log(this.port + " failed to ask " + port + " to accept " + operation + ".");
                }
            }
            Helper.log(this.port + " finished asking other servers to accept " + operation + ".");
            if (count > allPorts.size() / 2) {
                Helper.log(this.port + " succeed asking other servers to accept " + operation + ".");
                break;
            }
        }
        if (attempt == 3 && count <= allPorts.size() / 2) {
            Helper.log(this.port + " is not getting accepting consensus.");
            return new StoreOperationResult(false, "Accepting consensus could not be reached with " + count + " server accepted.");
        }
        System.out.println(attempt + "--------------" + count);

        // Commit phase
        Helper.log(this.port + " is asking other servers to commit " + operation + ".");
        // For each port, ask to commit operation
        for (Integer port : allPorts) {
            try {
                // Get the registry
                Registry registry = LocateRegistry.getRegistry("localhost", port);
                // Getting the Sort class object
                Server server = (Server) registry.lookup("PaxosServer");
                // Ask other server to commit operation and count response
                StoreOperationResult response = server.commit(operation, this.port);
                if (port.equals(this.port)) {
                    result = response;
                }
            } catch (NotBoundException | RemoteException e) {
                Helper.log(this.port + " failed to ask " + port + " to commit " + operation + ".");
            }
        }
        Helper.log(this.port + " finished asking other servers to commit " + operation + ".");

        return result;
    }

    @Override
    public void run() {

    }
}
