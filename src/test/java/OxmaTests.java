import org.junit.jupiter.api.Test;

/**
 * @author vlad333rrty
 */
public class OxmaTests extends OxmaTestBase {
    @Test
    public void testMultipleAssign() {
        runTest("assign_test.ox");
    }

    @Test
    public void testFunDecl() {
        runTestWithoutLogging("fun_decl_test.ox");
    }

    @Test
    public void testBubbleSort() {
        runTest("bubble_sort.ox");
    }

    @Test
    public void testForCycle() {
        runTest("for_cycle_test.ox");
    }
}
