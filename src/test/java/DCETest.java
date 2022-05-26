import kalina.compiler.OxmaCompiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author vlad333rrty
 */
public class DCETest {
    private static final OxmaTestBase testWithDCEPerformer = new OxmaTestWithDCEPerformer();
    private static final OxmaTestBase testWithoutOptimizationsPerformer = new OxmaTestWithoutOptimizationsPerformer();

    @Test
    public void testCase1() {
        runTest("dce/dce_test_case_1.ox");
    }

    @Test
    public void testCase2() {
        runTest("dce/dce_test_case_2.ox");
    }

    private void runTest(String fileName) {
        String withDCEResult = testWithDCEPerformer.runTestAndGetResult(fileName);
        String withoutDCEResult = testWithoutOptimizationsPerformer.runTestAndGetResult(fileName);
        Assertions.assertEquals(withDCEResult, withoutDCEResult);
    }

    private static class OxmaTestWithDCEPerformer extends OxmaTestBase {
        @Override
        protected OxmaCompiler getCompiler() {
            return new DCETestCompiler();
        }
    }

    private static class OxmaTestWithoutOptimizationsPerformer extends OxmaTestBase {
        @Override
        protected OxmaCompiler getCompiler() {
            return new NoOptimizationsCompiler();
        }
    }
}
