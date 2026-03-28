package carbonconfiglib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FilenameUtils;

public class LangConverter {
    public static void main(String[] args) throws IOException {
        for (Path path : Files.walk(Paths.get("carbonconfiglang")).filter(p -> p.toString().endsWith(".json")).collect(java.util.stream.Collectors.toList())) {
            generateJsonFromLang(FilenameUtils.removeExtension(path.getFileName().toString()));
        }
    }

    private static void generateJsonFromLang(String language) {
        Path target = Paths.get("src/main/resources/assets/carbonconfig/lang/" + language + ".lang");
        Path source = Paths.get("carbonconfiglang/" + language + ".json");

        try(BufferedReader reader = Files.newBufferedReader(source); BufferedWriter writer = Files.newBufferedWriter(target))
        {
            JsonObject toProcess = new JsonParser().parse(reader).getAsJsonObject();

            for(Map.Entry<String, JsonElement> entry : toProcess.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toList())) {
                writer.write(entry.getKey()+"="+entry.getValue().getAsString().replace("\n", "\\n"));
                writer.newLine();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void addMissingTranslationsToJson() {
        Path source = Paths.get("src/main/resources/assets/carbonconfig/lang/en_us.lang");
        Path target = Paths.get("carbonconfiglang/en_us.json");
        JsonObject result = new JsonObject();
        try(BufferedReader reader = Files.newBufferedReader(target))
        {
            Properties prop = new Properties();
            prop.load(Files.newInputStream(source));
            JsonObject toProcess = new JsonParser().parse(reader).getAsJsonObject();
            prop.forEach((K, V) -> {
                if(toProcess.has((String)K)) return;
                toProcess.addProperty((String)K, (String)V);
            });
            result = toProcess;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        try(JsonWriter writer = new JsonWriter(Files.newBufferedWriter(target))) {
            writer.setIndent("\t");
            Streams.write(result, writer);
        }
        catch(Exception e) { e.printStackTrace(); }
    }
}
