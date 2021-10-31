package org.ballerinalang.langserver.completions.builder;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder used to service template snippet completion item.
 *
 * @since 2.0.0
 */
public class ServiceTemplateCompletionItemBuilder {

    private ServiceTemplateCompletionItemBuilder() {

    }

    /**
     * Creates and returns a snippet type completion item.
     *
     * @param snippet       Text to be inserted
     * @param label         Label of the completion item
     * @param detail        Detail of the completion item
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(String snippet, String label, String detail) {
        return build(snippet, label, detail, new ArrayList<>());
    }

    /**
     * Creates and returns a snippet type completion item.
     *
     * @param snippet             Text to be inserted
     * @param label               Label of the completion item
     * @param detail              Detail of the completion item
     * @param additionalTextEdits Textedits consisting the range to be replaced by the completion item.
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(String snippet,
                                       String label, String detail,
                                       List<TextEdit> additionalTextEdits) {
        CompletionItem completionItem = new CompletionItem();
        String insertText = snippet;
        completionItem.setInsertText(insertText);
        completionItem.setLabel(label);
        completionItem.setDetail(detail);
        completionItem.setAdditionalTextEdits(additionalTextEdits);
        completionItem.setKind(CompletionItemKind.Snippet);
        return completionItem;
    }
}
