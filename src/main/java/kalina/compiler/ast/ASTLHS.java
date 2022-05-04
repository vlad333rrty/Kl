package kalina.compiler.ast;

import java.util.List;

import org.objectweb.asm.Type;

/**
 * @author vlad333rrty
 */
public record ASTLHS(List<String> variableNames, Type type) {
    public int size() {
        return variableNames.size();
    }

    @Override
    public String toString() {
        return type + " " + String.join(", ", variableNames);
    }
}
