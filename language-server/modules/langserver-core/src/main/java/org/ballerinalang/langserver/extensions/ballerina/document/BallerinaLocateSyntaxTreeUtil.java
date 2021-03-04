package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Range;

import java.util.Map;
import java.util.Optional;

/**
 * This is the BallerinaLocateSyntaxTreeUtil class for related utils in retrieving the location of syntax nodes
 * for a given selection.
 */
public class BallerinaLocateSyntaxTreeUtil {
    private static final String POSITION = "position";
    private static final String LOCATE_PATH_PROPERTY = "isNodePath";

    public static JsonElement mapNodePath(Range range, SyntaxTree syntaxTree, JsonElement syntaxTreeJson) {
        syntaxTreeJson.getAsJsonObject().addProperty(LOCATE_PATH_PROPERTY, true);
        Node node = CommonUtil.findNode(range, syntaxTree);

        if (node.kind() == SyntaxKind.LIST) {
            node = node.parent();
        }
        if (node == syntaxTree.rootNode()) {
            return syntaxTreeJson;
        }
        return findNodePath(syntaxTreeJson, syntaxTree, node);
    }

    private static JsonElement findNodePath (JsonElement syntaxTreeJson, SyntaxTree syntaxTree, Node node) {
        Optional<JsonElement> temp = Optional.of(syntaxTreeJson);

        while (temp.isPresent()) {
            temp = findChildren(syntaxTree, node, temp.get());
        }
        return syntaxTreeJson;
    }

    private static Optional<JsonElement> findChildren(SyntaxTree syntaxTree, Node node, JsonElement jsonNode) {
        for (Map.Entry<String, JsonElement> childEntry: jsonNode.getAsJsonObject().entrySet()) {
            if (childEntry.getValue().isJsonArray()) {
                for (JsonElement childProps: childEntry.getValue().getAsJsonArray()) {
                    Integer isPath = evaluateRange(syntaxTree, node, childProps,
                            childProps.getAsJsonObject().get(POSITION).getAsJsonObject());
                    if (isPath > 0) {
                        return Optional.of(childProps);
                    }
                    if (isPath < 0) {
                        return Optional.empty();
                    }
                }
            } else if (childEntry.getValue().isJsonObject() && childEntry.getValue().getAsJsonObject().has(POSITION)) {
                Integer isPath = evaluateRange(syntaxTree, node, childEntry.getValue(),
                            childEntry.getValue().getAsJsonObject().get(POSITION).getAsJsonObject());
                if (isPath > 0) {
                    return Optional.ofNullable(childEntry.getValue());
                }
                if (isPath < 0) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private static Integer evaluateRange(SyntaxTree syntaxTree, Node node, JsonElement jsonNode, JsonObject position) {
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(LinePosition.from(position.get("startLine").getAsInt(),
                position.get("startColumn").getAsInt()));
        int end = textDocument.textPositionFrom(LinePosition.from(position.get("endLine").getAsInt(),
                position.get("endColumn").getAsInt()));
        TextRange textRange = TextRange.from(start, end - start);

        if (textRange.intersectionExists(node.textRange())) {
            jsonNode.getAsJsonObject().addProperty(LOCATE_PATH_PROPERTY, true);
            if (textRange.equals(node.textRange())) {
                return -1;
            }
            return +1;
        }
        return 0;
    }
}
