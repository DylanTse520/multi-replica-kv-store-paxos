# Project 4: Multi-Replica Key-Value Store with Paxos

## Assignment overview

The purpose of the assignment is to extend what was built in project 3,
which replicated a key-value store server across 5 servers and used 2PC to
ensure consistency. In this assignment, we need to extend project 3 with
two requirements. First, to achieve fault-tolerant capability, we need to
implement Paxos roles a.k.a. Proposers, Acceptors, and Learners. The goal
is to focus on the Paxos implementation and algorithmic steps involved in
realizing consensus in event ordering. Second, to test fault-tolerant capability,
we need to configured acceptors to "fail" at random times. This can be achieved
by putting the acceptors to sleep from time to time.

## Technical impression

After evaluating the project requirement, I identified two major milestones
in project 4, which are implementing Paxos with Proposers, Acceptors, and
Learners and configuring acceptors to fail at random times and bring it back
to life. I first need to implement the Proposers, Acceptors, and Learners.
I first moved the capabilities originally coded in ServerImpl to each of the
roles, and then rewrote the Server interface to accept the three phases in Paxos:
prepare, accept and commit. With all these methods completed, I rewrote ServerImpl
to accept requests. ServerImpl will call proposer to propose the operation to
the servers, which first ask server to prepare and then ask server to accept
the operation. Each phase proposer would check if servers come back with a majority
of responses. If not, proposer will retry at most three time. Server will call
acceptor to prepare or to accept the operation. If things goes well and proposer
gets consensus, proposer will then ask servers to commit, which is achieved by
asking learner to commit. After these are completed, I have first milestone
completed. Now I need to make acceptor fail from time to time. I implement
this by putting acceptor to sleep randomly. If acceptor finds itself starting to
sleep, it returns false and always returns false during the sleep. If sleep time
ends, the acceptor wakes up, and it would take instructions and answer accordingly.

## Build and run the server cluster and clients

### Build the server and client

Under current directory, open a terminal and compile 10 java programs with the following commands:

```shell
javac Helper.java
javac Proposer.java
javac Acceptor.java
javac Learner.java
javac StoreOperation.java
javac StoreOperationResult.java
javac Server.java
javac ServerImpl.java
javac PaxosServer.java
javac PaxosClient.java
```

Create the jar file to execute with the following commands:

```shell
jar cmf PaxosServer.mf PaxosServer.jar PaxosServer.class Helper.class StoreOperation.class StoreOperationResult.class Server.class ServerImpl.class Proposer.class Acceptor.class Learner.class
jar cmf PaxosClient.mf PaxosClient.jar PaxosClient.class Helper.class StoreOperation.class StoreOperationResult.class Server.class PRE_POPULATION MINIMUM_OPERATION
```

### Run the server cluster and clients

Under current directory, open a terminal to start the RMI server cluster and enter five port numbers
for each of the servers with the following commands:

```shell
java -jar PaxosServer.jar
1001 1002 1003 1004 1005
```

Open several terminals and navigate to the same directory.
In each terminal, start one RMI client and enter the hostname and port number with the following commands.
This would start one RMI client and connect it to the given server.
You can connect each client to different servers to test the 2 phase commit for them.

```shell
java -jar PaxosClient.jar
localhost 1001
```

Then the client would ask if you want to prepopulate the server. Type "y" for yes, otherwise for no.

To put new records in the store, try "PUT 1 10".
To get a value by a key, try "GET 1".
To delete a key, try "DELETE 1".

### MISC

The PRE_POPULATION file is for storing the pre-population records.
The MINIMUM_OPERATION file is for storing the operations to be completed by the clients.
