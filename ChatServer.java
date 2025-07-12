// File: ChatServer.java
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1234;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("💬 Chat Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("✅ New client connected: " + socket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("❌ Server Error: " + e.getMessage());
        }
    }

    // Broadcast message to all connected clients
    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler ch : clientHandlers) {
            if (ch != sender) {
                ch.sendMessage(message);
            }
        }
    }

    // Remove disconnected client
    static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    // Inner class to handle each client
    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("👋 Welcome to the Chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("📨 Message: " + message);
                    ChatServer.broadcast(message, this);
                }
            } catch (IOException e) {
                System.out.println("⚠ Client disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ChatServer.removeClient(this);
            }
        }

        void sendMessage(String message) {
            out.println(message);
        }
    }
}