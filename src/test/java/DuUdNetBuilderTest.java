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
    public void testCase1() throws CFGConversionException, IncompatibleTypesException, ParseException, IOException, InterruptedException, TimeoutException {
        Map<DuUdNet.Definition, List<DuUdNet.InstructionCoordinates>> du = Map.of(
                new DuUdNet.Definition("a_0", 0, 0), List.of(new DuUdNet.InstructionCoordinates(0, 1)),
                new DuUdNet.Definition("a_1", 1, 0), List.of(new DuUdNet.InstructionCoordinates(2, 0)),
                new DuUdNet.Definition("a_3", 3, 0), List.of(new DuUdNet.InstructionCoordinates(2, 0)),
                new DuUdNet.Definition("a_2", 2, 0), List.of(new DuUdNet.InstructionCoordinates(2, 1)),
                new DuUdNet.Definition("b_0", 2, 1), List.of()
        );

        Map<DuUdNet.Use, List<DuUdNet.InstructionCoordinates>> ud = Map.of(
                new DuUdNet.Use("a_0", 0, 1), List.of(new DuUdNet.InstructionCoordinates(0, 0)),
                new DuUdNet.Use("a_1", 2, 0), List.of(new DuUdNet.InstructionCoordinates(1, 0)),
                new DuUdNet.Use("a_3", 2, 0), List.of(new DuUdNet.InstructionCoordinates(3, 0)),
                new DuUdNet.Use("a_2", 2, 1), List.of(new DuUdNet.InstructionCoordinates(2, 0))
        );

        compareDuUd(new DuUdNet(du, ud), "/test_case_1.ox");
    }

    @Test
    public void testCase2() throws CFGConversionException, IncompatibleTypesException, IOException, ParseException, InterruptedException, TimeoutException {
        Map<DuUdNet.Definition, List<DuUdNet.InstructionCoordinates>> du = Map.of(
                new DuUdNet.Definition("a_0", 0, 0), List.of(),
                new DuUdNet.Definition("b_0", 0, 1), List.of(new DuUdNet.InstructionCoordinates(0, 2)),
                new DuUdNet.Definition("b_1", 1, 0), List.of(new DuUdNet.InstructionCoordinates(2, 0)),
                new DuUdNet.Definition("b_3", 3, 0), List.of(new DuUdNet.InstructionCoordinates(2, 0)),
                new DuUdNet.Definition("b_2", 2, 0), List.of(new DuUdNet.InstructionCoordinates(2, 1))
        );

        Map<DuUdNet.Use, List<DuUdNet.InstructionCoordinates>> ud = Map.of(
                new DuUdNet.Use("b_0", 0, 2), List.of(new DuUdNet.InstructionCoordinates(0, 1)),
                new DuUdNet.Use("b_1", 2, 0), List.of(new DuUdNet.InstructionCoordinates(1, 0)),
                new DuUdNet.Use("b_3", 2, 0), List.of(new DuUdNet.InstructionCoordinates(3, 0)),
                new DuUdNet.Use("b_2", 2, 1), List.of(new DuUdNet.InstructionCoordinates(2, 0))
        );

        compareDuUd(new DuUdNet(du, ud), "/test_case_2.ox");
    }

    private void compareDuUd(DuUdNet expected, String testFileName) throws IOException, InterruptedException, TimeoutException, CFGConversionException, IncompatibleTypesException, ParseException {
        runLexer(DIRECTORY_NAME + testFileName);
        AbstractCFGNode root =
                cfgRootBuilderWithSSA.run("data/output.kl");
        var res = DuUdNetBuilder.buildDuUdNet(root);
        checkMapEquals(res.getDu(), expected.getDu());
        checkMapEquals(res.getUd(), expected.getUd());
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
