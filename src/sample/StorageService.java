package sample;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


public class StorageService {

    private final String FILE_NAME = "./storage.json";

    public void save(List<String> items) {
        writeToFile(items);
    }

    public List<String> load() {
        String content = readFromFile();

        Type listType = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(content, listType);
    }

    private void writeToFile(String content) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_NAME));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            System.out.println("Can not write to file" + e.getMessage());
        }

    }

    private void writeToFile(List<String> items) {
        Gson gson = new Gson();
        String json = gson.toJson(items);

        writeToFile(json);
    }

    private String readFromFile() {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(FILE_NAME)));
        } catch (IOException e) {
            System.out.println("Can not read file: " + FILE_NAME + ". " + e.getMessage());
        }

        return content;
    }

}
