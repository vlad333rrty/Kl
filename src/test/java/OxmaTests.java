import org.junit.jupiter.api.Test;

/**
 * @author vlad333rrty
 */
public class OxmaTests extends OxmaTestBase {
    @Test
    public void testMultipleAssign() {
        runTestAndLogResult("assign_test.ox");
    }

    @Test
    public void testFunDecl() {
        runTestWithoutLogging("fun_decl_test.ox");
    }

    @Test
    public void testBubbleSort() {
        runTestAndLogResult("bubble_sort.ox");
    }

    @Test
    public void testForCycle() {
        runTestAndLogResult("for_cycle_test.ox");
    }
}
