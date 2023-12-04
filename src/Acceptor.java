// Acceptor.java: the class implementing the acceptor

import java.util.ArrayList;
import java.util.HashMap;

public class Acceptor implements Runnable {

    // The proposal id this acceptor maintains
    private static int proposalId;

    // The key value store from the server
    HashMap<String, String> keyValueStore;

    // The port of the server having this acceptor
    Integer port;

    // The port numbers of all servers
    ArrayList<Integer> allPorts;

    // Is the acceptor sleeping
    boolean asleep;

    // The start time for sleeping
    long startTime;


    public Acceptor(HashMap<String, String> keyValueStore, Integer port, ArrayList<Integer> allPorts) {
        this.keyValueStore = keyValueStore;
        this.port = port;
        this.allPorts = allPorts;
        proposalId = 0;
        this.asleep = false;
    }

    public static void setProposalId(int proposalId) {
        Acceptor.proposalId = proposalId;
    }

    // Set the sleeping status
    public void setAsleep(boolean asleep) {
        this.asleep = asleep;
    }

    public boolean isAsleep() {
        // When sleeping
        if (asleep) {
            // Time to wake up
            if (System.currentTimeMillis() - startTime >= 3000) {
                Helper.log(this.port + " is waking up.");
                setAsleep(false);
            } else {
                Helper.log(this.port + " is still sleeping.");
                return true;
            }
        }
        // Randomly sleep the acceptor
        if (((int) (Math.random() * allPorts.size() * 2)) == this.allPorts.indexOf(this.port)) {
            Helper.log(this.port + " is going to sleep.");
            setAsleep(true);
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    // Prepare for an operation and see if it's valid
    public boolean prepare(int proposalId, Integer initPort) {
        Helper.log(this.port + " is preparing a request from " + initPort + ".");
        if (isAsleep()) {
            return false;
        }
        // Only return true when incoming proposal id is not smaller
        return proposalId >= Acceptor.proposalId;
    }

    // Accepting an operation and see if it's valid
    public boolean accept(int proposalId, StoreOperation operation, Integer initPort) {
        Helper.log(this.port + " is accepting a request from " + initPort + " for " + operation + ".");
        if (isAsleep()) {
            return false;
        }
        // Don't accept operation if incoming proposal id is smaller
        if (proposalId < Acceptor.proposalId) {
            return false;
        }
        // Check if an operation is valid
        switch (operation.operation()) {
            // GET or DELETE need to have valid key
            case "GET", "DELETE" -> {
                if (keyValueStore.containsKey(operation.key())) {
                    // Before returning, update the local proposal id
                    setProposalId(proposalId);
                    return true;
                }
            }
            // PUT need to have non-duplicate key
            case "PUT" -> {
                if (!keyValueStore.containsKey(operation.key())) {
                    // Before returning, update the local proposal id
                    setProposalId(proposalId);
                    return true;
                }
            }
            default -> throw new RuntimeException();
        }
        return false;
    }

    @Override
    public void run() {

    }
}
