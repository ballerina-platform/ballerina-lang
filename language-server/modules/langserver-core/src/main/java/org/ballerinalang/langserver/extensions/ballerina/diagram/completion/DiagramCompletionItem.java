package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

import org.eclipse.lsp4j.InsertTextFormat;

import java.util.List;

public class DiagramCompletionItem {
    private DiagramCompletionItemKind completionItemKind;

    private String insertText;

    private InsertTextFormat insertTextFormat;

    private List<SnippetParameter> snippetParameters;

    public DiagramCompletionItemKind getCompletionItemKind() {
        return completionItemKind;
    }

    public void setCompletionItemKind(DiagramCompletionItemKind completionItemKind) {
        this.completionItemKind = completionItemKind;
    }

    public String getInsertText() {
        return insertText;
    }

    public void setInsertText(String insertText) {
        this.insertText = insertText;
    }

    public void setSnippetParameters(List<SnippetParameter> params) {
        this.snippetParameters = params;
    }

    public void setInsertTextFormat(InsertTextFormat insertTextFormat) {
        this.insertTextFormat = insertTextFormat;
    }

    public InsertTextFormat getInsertTextFormat() {
        return insertTextFormat;
    }

    public List<SnippetParameter> getSnippetParameters() {
        return snippetParameters;
    }
}
