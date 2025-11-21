package Subtask_4_ProConServer;

// Οι βιβλιοθήκες που θα χρειαστώ
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    // η χωρητικότητα του server
    private static final int MAX_STORAGE = 1000;
    // Ορίζω τη διάρκεια εκτέλεσης του προγράμματος
    private static final long SERVER_LIFETIME = 60 * 1000;
    // Δημιουργώ ένα κενό αντικείμενο, το monitor του οποίου θα βάλουμε σε synchronized statement
    final Object storageLock = new Object();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int port;
    // Η μεταβλητή που κρατά την τιμή της αποθήκευσης
    int storage;

    // Ο constructor
    public Server(int port) {
        this.port = port;
        Random random = new Random();
        // Ορίζω μια τυχαία τιμή για την αρχική αποθήκευση, από 1 έως 1000
        storage = 1 + random.nextInt(MAX_STORAGE);
        System.out.println("Initial storage: " + storage);
        System.out.println("Max storage: " + MAX_STORAGE);
    }

    static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide a port as argument, in order to start the server.");
            return;
        }
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);

        // Add a shutdown hook to cleanly stop the server upon external signal (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                server.stop();
            }
        }));
        server.start();
    }

    public void start() {
        // δημιούργησε ένα Thread για να τρέχει το timer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Server scheduled to run for " + (SERVER_LIFETIME / 1000) + " seconds.");
                    // βάλε το νήμα για ύπνο για συγκεκριμένο χρόνο
                    Thread.sleep(SERVER_LIFETIME);
                    // Μόλις παρέλθει ο χρόνος, ενημέρωσε τον χρήστη
                    System.out.println("Ο χρόνος εκτέλεσης του προγράμματος ολοκληρώθηκε");
                    // Και κάλεσε την stop() για να θέσεις την AtomicBoolean σε False
                    stop();
                } catch (InterruptedException ignored) {
                    // Η διακοπή του νήματος είναι θεμιτή
                }
            }
        // τρέξε την start πάνω στο νήμα
        }, "Shutdown-Timer").start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            System.out.println("Waiting for clients...");

            // δέχεται τα αιτήματα σύνδεσης των client για όσο ο διακόπτης λειτουργίας running είναι `True`
            while (running.get()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected on port " + port + " from: " + clientSocket.getInetAddress().getHostAddress());
                // Για να τρέξουμε τον ClientHandler δημιουργώ ένα νέο νήμα
                // Στον ClientHandler περνάω τη θύρα του client, το στιγμιότυπο του Server και
                // τη μέγιστη χωρητικότητα για να ελέγχει εάν περνάμε το όριό της
                new Thread(new ClientHandler(clientSocket, this, MAX_STORAGE)).start();
            }
        } catch (IOException e) {
            // Σε περίπτωση που δεν μπορεί να συνδεθεί ο Server στη θύρα, ενώ ο διακόπτης είναι True
            if (running.get()) {
                System.err.println("Server error on port " + port + ": " + e.getMessage());
            // Ενώ αν είναι ηθελημένο (θέσαμε δηλαδή running = False)
            } else {
                System.out.println("Server shut down successfully on port " + port);
            }
        }
    }

    // Η λειτουργία της έγκειται στο να θέτει την τιμή της running ως False
    public void stop() {
        running.set(false);
    }
}