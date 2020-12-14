package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

final class Args {
    @Parameter(names = "-t", description = "Command name (type)")
    public String command;

    @Parameter(names = "-k", description = "Key of database cell")
    public String index;

    @Parameter(names = "-v", description = "Value of database cell (use for set command)")
    public String value;

    @Parameter(names = "-in", description = "File name to read data")
    public String fileName = null;
}

public class Main {

    private final static int SERVER_PORT = 32000;
    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static String PATH = "src/client/data/";
    //private final static String PATH = "/Users/Dead/IdeaProjects/";

    public static void main(String[] args) {

        Args argv = new Args();

        try {
            JCommander.newBuilder()
                    .addObject(argv)
                    .build()
                    .parse(args);
        } catch (Exception e) {
            System.out.println("Bad command!");
            System.exit(-1);
        }

        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.println("Client started!");

            String msg;
            if (argv.fileName == null) {
                Gson gson = new Gson();
                Map<String, String> inputArgs = new HashMap<>();

                inputArgs.put("type", argv.command);

                if (argv.index != null) {
                    inputArgs.put("key", argv.index);
                }
                if (argv.value != null) {
                    inputArgs.put("value", argv.value);
                }

                msg = gson.toJson(inputArgs);
            } else {
                msg = Files.readString(Paths.get(PATH + argv.fileName));
            }

            output.writeUTF(msg);
            System.out.println("Sent: " + msg);

            String receivedMsg = input.readUTF();
            System.out.println("Received: " + receivedMsg);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        
    }
}
