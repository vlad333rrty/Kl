import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import kalina.compiler.cfg.builder.nodes.AbstractCFGNode;
import kalina.compiler.cfg.exceptions.CFGConversionException;
import kalina.compiler.cfg.optimizations.DuUdNet;
import kalina.compiler.cfg.optimizations.DuUdNetBuilder;
import kalina.compiler.cfg.validator.IncompatibleTypesException;
import kalina.compiler.syntax.parser2.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author vlad333rrty
 */
public class DuUdNetBuilderTest extends OxmaTestBase {
    private static final CFGRootBuilderWithSSATest cfgRootBuilderWithSSA = new CFGRootBuilderWithSSATest();
    private static final String DIRECTORY_NAME = "/duUdTestSources";

    @Test
    public void test() throws CFGConversionException, IncompatibleTypesException, ParseException, IOException, InterruptedException, TimeoutException {
        Map<DuUdNet.Definition, List<DuUdNet.UseCoordinates>> du = Map.of(
                new DuUdNet.Definition("a_0", 0, 0), List.of(new DuUdNet.UseCoordinates(0, 1)),
                new DuUdNet.Definition("a_1", 1, 0), List.of(new DuUdNet.UseCoordinates(2, 0)),
                new DuUdNet.Definition("a_3", 3, 0), List.of(new DuUdNet.UseCoordinates(2, 0)),
                new DuUdNet.Definition("a_2", 2, 0), List.of(new DuUdNet.UseCoordinates(2, 1)),
                new DuUdNet.Definition("b_0", 2, 1), List.of()
        );

        Map<DuUdNet.Use, List<DuUdNet.DefinitionCoordinates>> ud = Map.of(
                new DuUdNet.Use("a_0", 0, 1), List.of(new DuUdNet.DefinitionCoordinates(0, 0)),
                new DuUdNet.Use("a_1", 2, 0), List.of(new DuUdNet.DefinitionCoordinates(1, 0)),
                new DuUdNet.Use("a_3", 2, 0), List.of(new DuUdNet.DefinitionCoordinates(3, 0)),
                new DuUdNet.Use("a_2", 2, 1), List.of(new DuUdNet.DefinitionCoordinates(2, 0))
        );

        runLexer(DIRECTORY_NAME + "/test_case_1.ox");
        AbstractCFGNode root =
                cfgRootBuilderWithSSA.run("data/output.kl");
        var res = DuUdNetBuilder.buildDuUdNet(root);
        checkMapEquals(res.getDu(), du);
        checkMapEquals(res.getUd(), ud);
    }

    private void checkMapEquals(Map<?, ?> x, Map<?, ?> y) {
        checkEmbedding(x, y);
        checkEmbedding(y, x);
    }

    private void checkEmbedding(Map<?, ?> x, Map<?, ?> y) {
        for (var entry : x.entrySet()) {
            var val = y.get(entry.getKey());
            Assertions.assertNotNull(val);
            Assertions.assertEquals(val, entry.getValue());
        }
    }
}
