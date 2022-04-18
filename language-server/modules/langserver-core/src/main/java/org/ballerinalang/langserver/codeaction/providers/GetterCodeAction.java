package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Code Action for getters.
 *
 * @since 2201.1.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class GetterCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Getter";
    public GetterCodeAction() {
        super(Arrays.asList(CodeActionNodeType.OBJECT_FIELD));
    }

    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context, NodeBasedPositionDetails posDetails) {

        if (!context.currentSyntaxTree().isPresent()) {
            return Collections.emptyList();
        }

        NonTerminalNode matchedNode = CommonUtil.findNode(new Range(context.cursorPosition(), context.cursorPosition()),
                context.currentSyntaxTree().get());
        String commandTitle = "Create a getter for " + ((ObjectFieldNode) matchedNode).fieldName().toString();
        String fieldName = String.valueOf(((ObjectFieldNode) matchedNode).fieldName());
        String typeName = String.valueOf(((ObjectFieldNode) matchedNode).typeName());
        String functionName = "get" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
        for (Node node: ((ClassDefinitionNode) matchedNode.parent()).members()) {
            if (node instanceof FunctionDefinitionNode &&
                    ((FunctionDefinitionNode) node).functionName().toString().equals(functionName)) {
                return Collections.emptyList();
            }
        }

        Position startPos = new Position(matchedNode.parent().lineRange().endLine().line(),
                ((ClassDefinitionNode) matchedNode.parent()).closeBrace().lineRange().startLine().offset());
        Range newTextRange = new Range(startPos, startPos);
        int offset = matchedNode.lineRange().startLine().offset();
        List<TextEdit> edits = CodeActionUtil.addGettersCodeActionEdits(fieldName, newTextRange,
                offset, typeName, context);
        return Collections.singletonList(createCodeAction(commandTitle, edits, context.fileUri()));
    }

    @Override
    public String getName() {
        return null;
    }
}
