package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.eclipse.lsp4j.Range;

import java.util.Map;
import java.util.Optional;

/**
 * This is the BallerinaSyntaxTreeNodePathUtil class for related utils in retrieving the location of syntax nodes
 * for a given selection.
 */

public class BallerinaSyntaxTreeNodePathUtil {
    public static JsonElement mapNodePath(Range range, SyntaxTree syntaxTree, JsonElement syntaxTreeJson) {
        Node node = BallerinaSyntaxTreeByRangeUtil.getNode(range, syntaxTree);
        return findNodePath(syntaxTreeJson, syntaxTree, node);
    }

    private static JsonElement findNodePath (JsonElement syntaxTreeJson, SyntaxTree syntaxTree, Node node) {
        Optional<JsonElement> temp = Optional.of(syntaxTreeJson);

        while (temp.isPresent()) {
            temp = findChildPath(syntaxTree, node, temp.get());
        }
        return syntaxTreeJson;
    }

    private static Optional<JsonElement> findChildPath(SyntaxTree syntaxTree, Node node, JsonElement jsonNode) {
        for (Map.Entry<String, JsonElement> childEntry: jsonNode.getAsJsonObject().entrySet()) {
            if (childEntry.getValue().isJsonArray()) {
                for (JsonElement childProps: childEntry.getValue().getAsJsonArray()) {
                    if (childProps.getAsJsonObject().has("position")) {
                        Boolean isPath = checkRange(syntaxTree, node, jsonNode,
                                childProps.getAsJsonObject().get("position").getAsJsonObject());
                        if (isPath) {
                            return Optional.of(childProps);
                        }
                    }
                }
            } else if (childEntry.getValue().isJsonObject()
                    && childEntry.getValue().getAsJsonObject().has("position")) {
                Boolean isPath = checkRange(syntaxTree, node, jsonNode,
                        childEntry.getValue().getAsJsonObject().get("position").getAsJsonObject());
                if (isPath) {
                    return Optional.ofNullable(childEntry.getValue());
                }
            }
        }

        return Optional.empty();
    }

    private static Boolean checkRange(SyntaxTree syntaxTree, Node node, JsonElement jsonNode, JsonObject position) {
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(LinePosition.from(position.get("startLine").getAsInt(),
                position.get("startColumn").getAsInt()));
        int end = textDocument.textPositionFrom(LinePosition.from(position.get("endLine").getAsInt(),
                position.get("endColumn").getAsInt()));
        TextRange textRange = TextRange.from(start, end - start);

        if (node.textRange().intersectionExists(textRange)) {
            jsonNode.getAsJsonObject().addProperty("isNodePath", true);
            return true;
        }

        return false;
    }
}
