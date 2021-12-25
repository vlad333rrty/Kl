package kalina.compiler.syntax.lexer.gen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import kalina.compiler.utils.FileUtils;

/**
 * @author vlad333rrty
 */
public class TokenTagGenerator {
    private static final Logger logger = Logger.getLogger(TokenTagGenerator.class.getName());

    private static final int START_LINE = 2; // skip first 2 lines
    private static final String OUTPUT_RELATIVE_PATH = "kalina/compiler/syntax/build";
    private static final String PATH_TO_CODE = "/src/main/java/";

    public static void main(String[] args) {
        logger.info("Starting TokenTagGenerator");

        String pathToCompiler = System.getProperty("user.dir");
        String outputPath = pathToCompiler.concat(PATH_TO_CODE).concat(OUTPUT_RELATIVE_PATH);
        File dir = new File(outputPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        List<String> files = new ArrayList<>(List.of(args));
        for (String s : files) {
            String path = outputPath.concat("/").concat(createGeneratedClassName(s)).concat(".java");
            File gen = new File(path);
            try {
                gen.createNewFile();
                FileUtils.writeToFile(path, generate(s));
            } catch (IOException e) {
                logger.warning("Failed to generate file for " + s + "\n\tError:" + e.getLocalizedMessage());
            }
        }
        logger.info("No errors. Stopping TokenTagGenerator");
    }

    private static String generate(String pathToSource) throws IOException {
        String className = createGeneratedClassName(pathToSource);
        String contents = FileUtils.readFile(pathToSource);
        List<String> input = prepareInput(contents);
        StringBuilder builder = new StringBuilder();
        builder.append("package kalina.compiler.syntax.build;\n\n");
        builder.append("public enum ").append(className).append(" {\n");
        for (String s : input) {
            String[] data = s.split(" ");
            builder.append('\t').append(data[0]).append(",\n");
        }
        builder.append("}");
        return builder.toString();
    }

    private static List<String> prepareInput(String input) {
        String[] lines = input.split("\n");
        List<String> result = new ArrayList<>();
        for (String s : lines) {
            if (s.chars().anyMatch(c -> !Character.isWhitespace(c))) {
                result.add(s.replaceAll("#define", "").trim().replaceAll(" {2,}", " "));
            }
        }

        return result.subList(START_LINE, result.size() - 1);
    }

    private static String createGeneratedClassName(String pathToSource) {
        Path path = Paths.get(pathToSource);
        String name = path.getFileName().toString();
        return name.substring(0, name.lastIndexOf("."));
    }
}
