package Subtask_3_ClientServer;

// Οι βιβλιοθήκες που θα χρειαστώ
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server {
    // Κατασκευάζω έναν πίνακα κατακερματισμού με:
    // - Κλειδί string και τιμή integer
    // - Μέγεθος του πίνακα 2^20
    int matrixSize = (int) Math.pow(2, 20);
    Hashtable<String, Integer> hashTable = new Hashtable<>(matrixSize);
    // Στη συνέχεια ανοίγει ένα socket στην πόρτα την οποία περνάμε ως παράμετρο στον constructor (π.χ. 8888, 9999, κλπ)
    // serverSocket: the socket where the server will be listening
    // waits for incoming client connection requests on a port
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    // to read-in the data
    InputStream incoming = null;
    BufferedReader bufferedReaderClient = null;
    String receivedTriplet = null;
    // to send back to the client
    PrintWriter outgoing = null;
    // ο constructor
    public Server(int port) throws IOException {
        // Create a server socket
        try {
            // Στη συνέχεια περιμένει να συνδεθεί κάποιος client
            // create a socket at a certain port
            serverSocket = new ServerSocket(port);
            // server is listening
            System.out.println("Server started, waiting for Client.");
        }   catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            // System.out: the console
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }

        try {
            // wait for the Client to call
            clientSocket = serverSocket.accept();
        }   catch (IOException e) {
            System.err.println("Connection failure at port " + port);
        }
        System.out.println("Accepted connection");
        // create the communication channel that will be used to receive data

        // Incoming stream
        // getInputStream() returns an incoming stream for this socket
        incoming = clientSocket.getInputStream();
        // InputStreamReader is a bridge from byte streams to character streams (the reverse of PrintWriter)
        bufferedReaderClient = new BufferedReader(new InputStreamReader(incoming));
        // Outgoing stream built on the client socket
        outgoing = new PrintWriter(clientSocket.getOutputStream(), true);
        // LOOP to handle multiple commands
        while (true) {
            String receivedTriplet = bufferedReaderClient.readLine();
            // null means the client has closed the connection
            if (receivedTriplet == null) {
                System.out.println("Client disconnected");
                break;
            }
            System.out.println("Received from client: " + receivedTriplet);
            String[] receivedTripletArray = receivedTriplet.split(" ");

        // I'll use switch, since I am checking one variable against multiple possible values
        switch (receivedTripletArray[0]) {
            case "0":
                if (receivedTripletArray[1].equals("0")) {
                    System.out.println("User entered 0 0 to terminate the connection");
                    System.out.println("\nHashtable: ");
                    System.out.println(hashTable);
                    break;
                } else {
                    System.out.println("You need to pass on two zeros to end the connection");
                    outgoing.println("0");
                    System.out.println("Sent to client: 0");
                    break;
                }
            // When the first string is '1', insert the values that follow to the hashtable
            case "1":
                try {
                    String key = receivedTripletArray[1];
                    Integer value = Integer.valueOf(receivedTripletArray[2]);

                    // Check if key already exists
                    if (hashTable.containsKey(key)) {
                        System.err.println("Key already exists: " + key);
                        outgoing.println("0");
                        System.out.println("Sent to client: 0");
                    } else {
                        hashTable.put(key, value);
                        outgoing.println("1");
                        System.out.println("Sent to client: 1");
                    }
                    break;
                } catch (Exception e) {
                    System.err.println("Failed to insert: " + e.getMessage());
                    outgoing.println("0");
                    System.out.println("Sent to client: 0");
                    break;
                }
            case "2":
                try {
                    hashTable.remove(receivedTripletArray[1]);
                    outgoing.println("1");
                    System.out.println("Sent to client: 1");
                    break;
                } catch (Exception e) {
                    System.err.println("Failed to insert " + port);
                    outgoing.println("0");
                    System.out.println("Sent to client: 0");
                    break;
                }
            case "3":
                int key = 0;
                try {
                    key = hashTable.get(receivedTripletArray[1]);
                    outgoing.println(key);
                    System.out.println("Sent to client: " + key);
                    break;
                } catch (Exception e) {
                    System.err.println("Failed to insert " + port);
                    outgoing.println("0");
                    System.out.println("Sent to client: 0");
                    break;
                }
            }
        };
        // The connection stays open the whole time until we close it
        clientSocket.close();
        outgoing.close();
        bufferedReaderClient.close();
        System.out.println("Client closed");
    }

    public static void main(String[] args) throws IOException {
        Integer port = null;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.err.println("Invalid port number: " + args[0]);
            // exit and return to the caller
            return;
        }
        try {
            Server server = new Server(port);
        } catch (IOException e) {
            System.err.println("Could not open server: " + e.getMessage());
        }
    }
}



