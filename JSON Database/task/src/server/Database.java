package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private final String fileName = "src/server/data/db.json";
    //private final String fileName = "/Users/Dead/IdeaProjects/db.json";
    private Map<String, String> data;

    private void readDataFile() {
        try (FileInputStream fis = new FileInputStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            data = (Map<String, String>) ois.readObject();
        } catch (Exception e) {
            data = new HashMap<>();
        }
    }

    private void saveDataFile() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(data);
        }
    }

    public DBResponse get(Object key) {
        readDataFile();

        DBResponse response = null;

        //if (key.startsWith("[")) {
        if (key instanceof ArrayList) {
            String[] fullKey = getFullKey((ArrayList)key);

            if (data.containsKey(fullKey[0])) {
                JsonElement currentObject = JsonParser.parseString(data.get(fullKey[0]));

                for (int i = 1; i < fullKey.length; i++) {
                    if (currentObject.getAsJsonObject().has(fullKey[i])) {
                        JsonElement element = currentObject.getAsJsonObject().get(fullKey[i]);

                        if (element.isJsonPrimitive()) {
                            currentObject = element.getAsJsonPrimitive();
                        } else {
                            currentObject = element.getAsJsonObject();
                        }
                    } else {
                        currentObject = null;
                        break;
                    }
                }

                if (currentObject != null) {
                    //response = new DBResponse("OK", currentObject.isJsonPrimitive() ? currentObject.getAsString() : currentObject.getAsJsonObject().toString());
                    response = new DBResponse("OK", currentObject);
                }
            }
        } else {
            if (data.containsKey(key)) {
                response = new DBResponse("OK", data.get(key));
            }
        }

        return response != null ? response : new DBResponse("ERROR", new IllegalArgumentException("No such key"));
    }

    public DBResponse set(Object key, String value) {
        readDataFile();

        DBResponse response;

        //if (key.startsWith("[")) {
        if (key instanceof ArrayList) {
            String[] fullKey = getFullKey((ArrayList)key);

            JsonElement json;
            JsonElement currentObject;

            int i = 0;
            if (data.containsKey(fullKey[0])) {
                json = JsonParser.parseString(data.get(fullKey[0]));
                currentObject = json;

                JsonElement element;

                i++;
                while (i < fullKey.length - 1) {

                    if (currentObject.getAsJsonObject().has(fullKey[i])) {
                        element = currentObject.getAsJsonObject().get(fullKey[i]);

                        if (element.isJsonPrimitive()) {
                            currentObject = element.getAsJsonPrimitive();
                        } else {
                            currentObject = element.getAsJsonObject();
                        }
                    } else {
                        break;
                    }

                    i++;
                }
            } else {
                json = new JsonObject();
                currentObject = json;
            }

            //System.out.println("i = " + i + ", length = " + fullKey.length);
            if (i < fullKey.length) {
                createJsonStructure(currentObject.getAsJsonObject(), fullKey, i, value);
            }

            data.put(fullKey[0], json.toString());
        } else {
            data.put((String)key, value);
        }

        try {
            saveDataFile();
            response = new DBResponse("OK");
        } catch (IOException e) {
            response = new DBResponse("ERROR", e);
        }

        return response;
    }

    public DBResponse delete(Object key) {
        readDataFile();

        DBResponse response = null;
        boolean isOk = true;

        //if (key.startsWith("[")) {
        if (key instanceof ArrayList) {
            String[] fullKey = getFullKey((ArrayList)key);

            if (data.containsKey(fullKey[0])) {
                JsonElement root = JsonParser.parseString(data.get(fullKey[0]));
                JsonElement lastObject = root;
                JsonElement currentObject = root;

                for (int i = 1; i < fullKey.length; i++) {
                    if (currentObject.getAsJsonObject().has(fullKey[i])) {
                        lastObject = currentObject;

                        JsonElement element = currentObject.getAsJsonObject().get(fullKey[i]);
                        if (element.isJsonPrimitive()) {
                            currentObject = element.getAsJsonPrimitive();
                        } else {
                            currentObject = element.getAsJsonObject();
                        }
                    } else {
                        currentObject = null;
                        break;
                    }
                }

                if (currentObject == null) {
                    isOk = false;
                } else {
                    if (fullKey.length == 1) {
                        data.remove(fullKey[0]);
                    } else {
                        lastObject.getAsJsonObject().remove(fullKey[fullKey.length - 1]);
                        data.put(fullKey[0], root.toString());
                    }
                }
            }
        } else {
            if (data.containsKey(key)) {
                data.remove(key);
            } else {
                isOk = false;
            }
        }

        if (isOk) {
            try {
                saveDataFile();
                response = new DBResponse("OK");
            } catch (IOException e) {
                response = new DBResponse("ERROR", e);
            }
        }

        return response != null ? response : new DBResponse("ERROR", new IllegalArgumentException("No such key"));
    }

    private void createJsonStructure(JsonObject json, String[] fullKey, int index, String value) {
        JsonObject newObject = new JsonObject();

        if (index == fullKey.length - 1) {
            json.addProperty(fullKey[index], value);
        } else {
            createJsonStructure(newObject, fullKey, index + 1, value);
        }
    }


    private String[] getFullKey(ArrayList key) {
        String[] result = new String[key.size()];
        for (int i = 0; i < key.size(); i++) {
            result[i] = (String)key.get(i);
        }

        return result;
    }

}
