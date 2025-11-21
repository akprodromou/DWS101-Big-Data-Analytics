package Subtask_4_ProConServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Consumer implements Runnable {
    private final String host = "localhost";
    private final List<Integer> serverPorts = Arrays.asList(8881, 8882, 8883);
    private final Random random = new Random();

    static void main(String[] args) {
        int numConsumers = 3;
        for (int i = 0; i < numConsumers; i++) {
            new Thread(new Consumer(), "Consumer-" + (i + 1)).start();
        }
    }

    @Override
    // η run() εκτελείται όταν καλούμε την start σε ένα αντικείμενο Thread
    public void run() {
        // το όνομα ενός από τα 3 νήματα που δημιουργήσαμε
        String threadName = Thread.currentThread().getName();
        while (true) {
            // διάλεξε μια τυχαία θέση index της λίστας με τις θύρες του server
            int port = serverPorts.get(random.nextInt(serverPorts.size()));

            // η εντολή θα έχει ένα prefix (εδώ SUB, στον producer ADD), ακολουθούμενη από τον αριθμό που θέλουμε να προστεθεί
            String command;
            // Τυχαίος αριθμός μεταξύ 10–100
            int consumeAmount = 10 + random.nextInt(91);
            command = "SUB " + consumeAmount;

            try (
                 // αυτά είναι τα resources μου στο Try with Resource
                 Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // ενημέρωσε το ClientHandler πως θα αφαιρέσουμε
                out.println(command);
                // και αποθήκευσε την απάντηση από τον ClientHandler
                String response = in.readLine();
                // αν δεν έχεις απάντηση, σπάσε το loop
                if (response == null) {
                    System.out.println(threadName + " → Server closed connection. Stopping.");
                    break;
                }
                // Αν έχουμε απάντηση, τύπωσέ την
                System.out.println(threadName + " → Connected to port " + port +
                        ", tried " + command + " → Server says: " + response);
            // εάν ένα από τα Resources μου δεν τηρούνται
            } catch (IOException e) {
                System.err.println(threadName + " → Error connecting to port " + port + ": " + e.getMessage());
                break;
            }
            // Μείνε σε παύση για τυχαίο αριθμό 1–10 δευτερόλεπτα και συνέχισε
            try {
                Thread.sleep((1 + random.nextInt(10)) * 1000L);
            } catch (InterruptedException ignored) {
                // Η διακοπή του νήματος είναι θεμιτή
            }
        }
    }
}