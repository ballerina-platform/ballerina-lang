package org.ballerinalang.langserver.extensions.ballerina.diagram.completion.providers;

import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.AbstractDiagramCompletionProvider;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionContext;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionItem;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionItemKind;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.SnippetParameter;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JavaSPIService("org.ballerinalang.langserver.extensions.ballerina.diagram.completion.spi.DiagramCompletionProvider")
public class BlockStatementNodeContext extends AbstractDiagramCompletionProvider<BlockStatementNode> {
    public BlockStatementNodeContext(List<Class<BlockStatementNode>> attachmentPoints) {
        super(Collections.singletonList(BlockStatementNode.class));
    }

    @Override
    public List<DiagramCompletionItem> getCompletions(DiagramCompletionContext context, Node node) {
        DiagramCompletionItem completionItem = new DiagramCompletionItem();
        completionItem.setCompletionItemKind(DiagramCompletionItemKind.IF);
        completionItem.setInsertText("if (${condition}) {" + CommonUtil.LINE_SEPARATOR + "}");
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);

        DiagramCompletionItem typeGuard = new DiagramCompletionItem();
        String typeDesc1 = "int";
        String typeDesc2 = "string";
        String typeGuardInsertText = "if (${varName} is " + typeDesc1 + ") {"
                + CommonUtil.LINE_SEPARATOR + "} else if (${varName} is " + typeDesc2 + ") {"
                + CommonUtil.LINE_SEPARATOR + "} else {" + CommonUtil.LINE_SEPARATOR + "}";
        typeGuard.setInsertText(typeGuardInsertText);

        List<SnippetParameter> params1 = new ArrayList<>();
        List<SnippetParameter> paramsTypeGuard = new ArrayList<>();
        SnippetParameter param1 = new SnippetParameter();
        param1.setKind(SnippetParameter.ParameterKind.Input);
        params1.add(param1);
        
        completionItem.setSnippetParameters(params1);
        return Collections.singletonList(completionItem);
    }
}
