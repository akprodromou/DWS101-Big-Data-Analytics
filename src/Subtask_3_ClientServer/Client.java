package Subtask_3_ClientServer;

// Οι βιβλιοθήκες που θα χρειαστώ
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;

public class Client {
    // Ο client αρχίζει τη λειτουργία του και προσπαθεί να συνδεθεί με το server στην πόρτα που έχουμε ορίσει
    // declare variables
    Socket clientSocket = null;
    // to send data
    OutputStream output = null;
    // the string read-in from the command prompt
    String cmd = null;
    // I'll use BufferedReader to read-in chunks of code to involve a single system call
    // and avoid switching between JVM and system kernel
    BufferedReader bufferedReaderConsole = null;
    // and a second buffer reader, to read-in server responses
    BufferedReader bufferedReaderServer = null;
    // η τριάδα που πληκτρολογεί ο χρήστης
    String triplet = null;
    // PrintWriter converts the primitive data (int, float, char, etc.) into text format
    // It takes any OutputStream and gives you convenient text-output methods, such as print(), println(), printf()
    PrintWriter outgoing = null;
    DataOutputStream dataOutputStream = null;
    // to receive from the server
    InputStream incoming = null;
    // Το μήνυμα από τον client
    String receivedString = null;
    // Πάλι, η πόρτα θα πρέπει να περνάει ως παράμετρος στον constructor του client
    // IOException: an I/O exception of some sort has occurred
    public Client(String host, int port) throws IOException {
        try {
            // Create the socket
            // attempt to make a connection to the server
            // Connect the socket to the specific host and port
            clientSocket = new Socket(host, port);
            System.out.println("Connected to server");
            // create the communication channel that will be used to send data
            // getOutputStream() returns an output stream for this socket
            // Because the socket is wrapped inside the Print Wrapper, when I use the Print Wrapper methods
            // on the outgoing objects, it's like having an open connection and therefore automatically sends the arguments to the receiver.
            // setting to true auto-flushes the data
            outgoing = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Connection failure at port " + port);
            System.out.println("Connection attempt failed");
            // show the error to the caller by rethrowing
            throw e;
        }
        // Read-in user input
        // by wrapping the System.in (standard input stream) in an InputStreamReader which is
        // wrapped in a BufferedReader, we can read input from the user in the command line
        // System.in is a standard input stream that always exists so no try-catch needed
        bufferedReaderConsole = new BufferedReader(new InputStreamReader(System.in));

        // Incoming stream from server
        // getInputStream() returns an incoming stream for this socket
        incoming = clientSocket.getInputStream();
        bufferedReaderServer = new BufferedReader(new InputStreamReader(incoming));

        // Send the user input to the server
        // Ο client στέλνει στον server μία τριάδα τιμών (Α, Β, Γ), όπου το Α έχει μία από τις
        // τιμές 0 (τέλος επικοινωνίας), 1 (insert), 2 (delete) και 3 (search), το Β είναι
        // ένας ακέραιος αριθμός που δηλώνει το κλειδί και το Γ ένας ακέραιος αριθμός που
        // δηλώνει την τιμή έχει το κλειδί
        // Η σύνδεση καταργείται όταν ο client στείλει την εντολή (0,0)
        System.out.print("Enter values: ");
        while (true) {
            triplet = bufferedReaderConsole.readLine();
            if (triplet == null) {
                System.err.println("Input stream closed");
                break;
            }
            String[] tripletArray = triplet.split(" ");
            if (tripletArray.length < 2) {
                System.out.println("Please enter at least 2 digits (A B or A B C)");
                System.out.print("Enter values: ");
                continue;
            }
            if (tripletArray[0].equals("0") && tripletArray[1].equals("0")) {
                System.out.println("Sent to server: " + triplet);
                // Send the command to the server
                outgoing.println(triplet);
                break;
            }
            if (tripletArray[0].equals("1") && tripletArray.length < 3) {
                System.out.println("Please enter 2 digits following 1 to insert");
            }
            outgoing.println(triplet);
            System.out.println("Sent to server: " + triplet);
            receivedString = bufferedReaderServer.readLine();
            System.out.println("Received from server: " + receivedString);
            // prompt for the next input
            System.out.print("Enter values: ");
        }
        clientSocket.close();
        outgoing.close();
        bufferedReaderConsole.close();
        bufferedReaderServer.close();
        System.out.println("Client closed");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <port>");
            System.out.println("Example: java Client localhost 8888");
            return;
        }
        String host = args[0];
        Integer port = null;

        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Invalid port number: " + args[1]);
            return;
        }

        try {
            Client client = new Client(host, port);
        } catch (IOException e) {
            System.err.println("Could not open connection to server: " + e.getMessage());
        }
    }
}


