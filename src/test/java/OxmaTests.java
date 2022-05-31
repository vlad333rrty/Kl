import org.junit.jupiter.api.Assertions;
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

    @Test
    public void testArraysDecl() {
        runTestWithoutLogging("arrays_test.ox");
    }

    @Test
    public void testLinearSort() {
        runTestAndLogResult("linear_sort.ox");
    }

    @Test
    public void testPrivateMemberAccessThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> runTestWithoutLogging("private_member_test.ox"));
    }

    @Test
    public void testNonStaticMemberAccessInStaticContext() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> runTestWithoutLogging("static_context_test.ox"));
    }

    @Test
    public void testNonStaticMemberAccessInStaticContext2() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> runTestWithoutLogging("static_context_test_1.ox"));
    }

    @Test
    public void testConstantFolding() {
        runTestAndLogResult("constant_folding_1.ox");
    }
}
