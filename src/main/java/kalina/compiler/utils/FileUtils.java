package kalina.compiler.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author vlad333rrty
 */
public final class FileUtils {
    public static String readFile(String path) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.substring(0, builder.length() - 1);
    }

    public static void writeToFile(String path, String data) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(path))) {
            out.write(data);
        }
    }
}
