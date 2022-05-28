import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import kalina.compiler.OxmaCompiler;
import kalina.compiler.utils.FileUtils;
import org.junit.jupiter.api.Assertions;

/**
 * @author vlad333rrty
 */
abstract class OxmaTestBase {
    private static final int LEXER_ERROR_CODE = 255;
    private static final int TIME_TO_WAIT_SECONDS = 10;
    private static final OxmaMain compiler = new OxmaMain();
    private static final String ERROR_DURING_EXECUTION_MESSAGE_FORMAT = "Error during execution of the generated java program. Log:\n%s";

    public void runLexer(String fileName) throws IOException, TimeoutException, InterruptedException {
        URL url = OxmaTests.class.getResource(fileName);
        assert url != null;
        executeCommand(
                "./oxma " + url.getFile(),
                "Error during lexical analysis. Lexer log:\n%s",
                LEXER_ERROR_CODE,
                System.out::println);
    }

    private void executeCommand(String command, String format, int errorCode, Consumer<String> logFun) throws InterruptedException, TimeoutException, IOException {
        Process process = Runtime.getRuntime().exec(command);

        if (!process.waitFor(TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS)) {
            throw new TimeoutException();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (process.exitValue() == errorCode) {
                throw new RuntimeException(String.format(format, builder));
            } else {
                logFun.accept(builder.toString());
            }
        }
    }

    public void runTestAndLogResult(String fileName) {
        runTest(fileName, true);
    }

    public void runTestWithoutLogging(String fileName) {
        runTest(fileName, false);
    }

    private void runTest(String fileName, boolean isLoggingEnabled) { // todo refactor
        try {
            runLexer(fileName);
            getCompiler().run("data/output.kl");
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
            String resultFile = "results/" + fileNameWithoutExtension + ".txt";
            executeCommand(
                    "java Test",
                    "Error during execution of the generated java program. Log:\n%s",
                    1,
                    getLogFun(resultFile, isLoggingEnabled)
            );
            String sampleFile = "samples/" + fileNameWithoutExtension + ".txt";
            if (isLoggingEnabled) {
                compareResults(sampleFile, resultFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String runTestAndGetResult(String fileName) {
        try {
            runLexer(fileName);
            getCompiler().run("data/output.kl");
            StringBuilder builder = new StringBuilder();
            executeCommand(
                    "java Test",
                    ERROR_DURING_EXECUTION_MESSAGE_FORMAT,
                    1,
                    builder::append
            );
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void compareResults(String pathToSample, String pathToResult) throws IOException {
        URL sample = OxmaTestBase.class.getResource(pathToSample);
        assert sample != null;
        URL result =  OxmaTestBase.class.getResource(pathToResult);
        assert result != null;

        String sampleContent = FileUtils.readFile(sample.getFile());
        String resultContent = FileUtils.readFile(result.getFile());

        Assertions.assertEquals(sampleContent, resultContent);
    }

    private Consumer<String> getLogFun(String fileName, boolean shouldSaveResult) {
        if (!shouldSaveResult) {
            return str -> {
            };
        }
        return str -> {
            try {
                URL url = OxmaTestBase.class.getResource(fileName);
                assert url != null;
                FileUtils.writeToFile(url.getFile(), str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    protected OxmaCompiler getCompiler() {
        return compiler;
    }
}
