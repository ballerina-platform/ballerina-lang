package org.ballerinalang.langserver.completions.providers.context.util;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;

/**
 * Utilities for the class definition context.
 * 
 * @since 2.0.0
 */
public class ClassDefinitionNodeContextUtil {
    private ClassDefinitionNodeContextUtil() {
    }

    public static boolean onSuggestInitFunction(Node node) {
        if (node.kind() != SyntaxKind.CLASS_DEFINITION) {
            return false;
        }

        ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) node;
        return classDefinitionNode.members().stream()
                .filter(member -> member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION)
                .map(member -> (FunctionDefinitionNode) member)
                .noneMatch(funcDef -> ItemResolverConstants.INIT.equals(funcDef.functionName().text()));
    }
}
