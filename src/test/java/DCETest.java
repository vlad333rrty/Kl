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

    @Test
    public void testCase3() {
        runTest("dce/dce_test_case_3.ox");
    }

    @Test
    public void testCase4() {
        runTest("dce/dce_test_case_4.ox");
    }

    @Test
    public void testCase5() {
        runTest("dce/dce_test_case_5.ox");
    }

    @Test
    public void testCase6() {
        runTest("dce/dce_test_case_6.ox");
    }

    @Test
    public void testCase7() {
        runTest("dce/dce_test_case_7.ox");
    }

    @Test
    public void testCase8() {
        runTest("dce/dce_test_case_8.ox");
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
