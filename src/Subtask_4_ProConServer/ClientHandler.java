package Subtask_4_ProConServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private final int maxStorage;
    // ο constructor
    public ClientHandler(Socket clientSocket, Server server, int maxStorage) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.maxStorage = maxStorage;
    }

    @Override
    public void run() {
        try (
            // κανάλι εισερχομένων
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // και εξερχομένων
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String command;
            // Read commands until the client closes the connection or sends EOF
            while ((command = in.readLine()) != null) {
                handleCommand(command, out);
            }
        } catch (IOException e) {
            // This is expected when a client abruptly disconnects
            System.err.println("Client disconnected or error: " + e.getMessage());
        } finally {
            try {
                // Ensure the socket is closed even if an exception occurred
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException ignored) {
                // Ignore errors during socket closing
            }
        }
    }

    private void handleCommand(String command, PrintWriter out) {
        String[] parts = command.split(" ");
        if (parts.length == 0) return;

        switch (parts[0]) {
            case "ADD":
                if (parts.length < 2) {
                    out.println("ERROR: Missing value");
                    return;
                }
                int addValue = Integer.parseInt(parts[1]);
                synchronized (server.storageLock) {
                    // Use the maxStorage limit for production
                    if (server.storage + addValue > maxStorage) {
                        out.println("FULL");
                    } else {
                        server.storage += addValue;
                        out.println("OK");
                    }
                }
                break;
            case "SUB":
                if (parts.length < 2) {
                    out.println("ERROR: Missing value");
                    return;
                }
                int subValue = Integer.parseInt(parts[1]);
                synchronized (server.storageLock) {
                    // Check for underflow/empty condition
                    if (server.storage - subValue < 0) {
                        out.println("EMPTY");
                    } else {
                        server.storage -= subValue;
                        out.println("OK");
                    }
                }
                break;
            case "GET":
                synchronized (server.storageLock) {
                    out.println(server.storage);
                }
                break;
            default:
                out.println("UNKNOWN");
        }
    }
}