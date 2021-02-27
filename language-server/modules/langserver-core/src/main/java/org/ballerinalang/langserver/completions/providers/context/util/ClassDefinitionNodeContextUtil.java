package org.ballerinalang.langserver.completions.providers.context.util;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.stream.Collectors;

/**
 * Utilities for the class definition context.
 * 
 * @since 2.0.0
 */
public class ClassDefinitionNodeContextUtil {
    private ClassDefinitionNodeContextUtil() {
    }
    
    public static boolean onSuggestResourceSnippet(Node node) {
        if (node.kind() == SyntaxKind.SERVICE_DECLARATION) {
            return true;
        }
        if (node.kind() == SyntaxKind.CLASS_DEFINITION) {
            return ((ClassDefinitionNode) node).classTypeQualifiers().stream()
                    .map(Token::text).collect(Collectors.toList())
                    .contains("service");
        }
        
        return false;
    }
}
