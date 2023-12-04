// Learner.java: the class implementing the learner in Paxos

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Learner implements Runnable {

    // The read write lock to allow synchronization
    ReadWriteLock rwl;

    // The key value store from the server
    HashMap<String, String> keyValueStore;

    // The port of the server having this learner
    Integer port;

    public Learner(HashMap<String, String> keyValueStore, Integer port) {
        this.rwl = new ReentrantReadWriteLock();
        this.keyValueStore = keyValueStore;
        this.port = port;
    }

    // Get the value from the key value store
    private StoreOperationResult getValue(String key) {
        // Lock the lock before read
        rwl.readLock().lock();
        // Get the value from the store
        String value = keyValueStore.get(key);
        // Creating the result object
        StoreOperationResult result;
        if (value == null) {
            // If the key is not in the store
            result = new StoreOperationResult(false, "Key does not exist in map");
        } else {
            // Otherwise return the value
            result = new StoreOperationResult(true, value);
        }
        Helper.log(result.toString());
        // Unlock the lock after read
        rwl.readLock().unlock();
        return result;
    }

    // Put the value into the key value store
    private StoreOperationResult putValue(String key, String value) {
        // Lock the lock before write
        rwl.writeLock().lock();
        // Store the key value in the store
        keyValueStore.put(key, value);
        // Creating the result object
        StoreOperationResult result = new StoreOperationResult(true, "Put " + key + ":" + value);
        Helper.log(result.toString());
        // Unlock the lock after write
        rwl.writeLock().unlock();
        return result;
    }

    // Delete the value from the key value store
    private StoreOperationResult deleteValue(String key) {
        // Lock the lock before write
        rwl.writeLock().lock();
        // Creating the result object
        StoreOperationResult result;
        if (keyValueStore.containsKey(key)) {
            // If the key is in the store, remove the key
            keyValueStore.remove(key);
            result = new StoreOperationResult(true, "Deleted " + key);
        } else {
            // Otherwise log error
            result = new StoreOperationResult(false, "Key does not exist in map");
        }
        Helper.log(result.toString());
        // Unlock the lock after write
        rwl.writeLock().unlock();
        return result;
    }

    // Commit the operation to the key value store
    public StoreOperationResult commit(StoreOperation operation, Integer initPort) {
        Helper.log(this.port + " is committing a request from " + initPort + " for " + operation + ".");
        // Creating the result object
        StoreOperationResult result;
        // Commit the operation
        switch (operation.operation()) {
            case "GET" -> result = getValue(operation.key());
            case "PUT" -> result = putValue(operation.key(), operation.value());
            case "DELETE" -> result = deleteValue(operation.key());
            default -> throw new RuntimeException();
        }
        Helper.log(this.port + " committed a request from " + initPort + " for " + operation + ".");
        return result;
    }

    @Override
    public void run() {

    }
}
