package kalina.compiler.cfg.traverse;

import kalina.compiler.cfg.data.TypeChecker;
import kalina.compiler.syntax.parser.data.AbstractLocalVariableTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public class Assert {
    private static final Logger logger = LogManager.getLogger(Assert.class);

    public static boolean assertIsValidDeclarationType(Type type, TypeChecker typeChecker) {
        if (type.getSort() == Type.VOID || !typeChecker.hasType(type.getClassName())) {
            logger.error("Wrong declaration type {}", type.getClassName());
            return false;
        }
        return true;
    }

    public static void assertMultipleVariableDeclarations(String varName, AbstractLocalVariableTable localVariableTable) {
        if (localVariableTable.hasVariableGlobal(varName)) {
            logger.error("Multiple variable declarations for {}", varName);
            throw new IllegalArgumentException("Multiple variable declarations for " + varName);
        }
    }

    public static void isArray(Type type) {
        if (type.getSort() != Type.ARRAY) {
            logger.error("Expected array, got {}", type);
            throw new IllegalArgumentException("Expected array, got " + type);
        }
    }
}
