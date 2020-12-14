package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;

final class Args {
    public String type;
    public Object key;
    public Object value;
}

public class Main {

    private final static int PORT = 32000;

    public static void main(String[] args) {
        Database database = new Database();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (
                ServerSocket serverSocket = new ServerSocket(PORT, 10)
        ) {
            System.out.println("Server started!");

            Gson gson = new Gson();

            while (!serverSocket.isClosed()){
                executor.submit(() -> {
                    try (
                            Socket socket = serverSocket.accept();
                            DataInputStream input = new DataInputStream(socket.getInputStream());
                            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                    ) {
                        String msg = input.readUTF();

                        Args command = gson.fromJson(msg, Args.class);

                        DBResponse response;
                        if (!"exit".equals(command.type.toLowerCase())) {
                            try {
                                switch (command.type.toLowerCase()) {
                                    case "set":
                                        Object value = command.value instanceof String ? command.value : gson.toJson(command.value);
                                        response = database.set(command.key, (String) value);
                                        break;
                                    case "get":
                                        response = database.get(command.key);
                                        break;
                                    case "delete":
                                        response = database.delete(command.key);
                                        break;
                                    default:
                                        response = new DBResponse("ERROR", new IllegalArgumentException("Unknown command"));
                                }

                            } catch (Exception e) {
                                response = new DBResponse("ERROR", e);
                            }
                        } else {
                            response = new DBResponse("OK");
                        }

                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("response", response.getResponse());
                        if ("ERROR".equals(response.getResponse())) {
                            result.put("reason", response.getReason().getMessage());
                        } else {
                            if (response.getValue() != null) {
                                result.put("value", response.getValue());
                            }
                        }

                        String outputMsg = gson.toJson(result);
                        output.writeUTF(outputMsg);

                        if ("exit".equals(command.type.toLowerCase())) {
                            serverSocket.close();
                        }
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        executor.shutdown();
    }

}
