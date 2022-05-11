package kalina.compiler.syntax.parser2.data;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public interface IndexGenerator {
    int getNewIndex(Type type);
}
