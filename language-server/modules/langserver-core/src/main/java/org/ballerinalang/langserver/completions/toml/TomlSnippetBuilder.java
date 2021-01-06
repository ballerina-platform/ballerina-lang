package org.ballerinalang.langserver.completions.toml;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

/**
 * Builder for building toml snippets.
 *
 * @since 2.0.0
 */
public class TomlSnippetBuilder {

    public static CompletionItem getContainerImageName() {
        KeyValuePair keyValuePair = new KeyValuePair("name", "", 1, ValueType.STRING);
        String s = keyValuePair.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("name");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("name");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }

    public static CompletionItem getContainerImageRepository() {
        KeyValuePair keyValuePair = new KeyValuePair("repository", "local", 1, ValueType.STRING);
        String s = keyValuePair.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("repository");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("repository");
        //item.setSortText("1");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }

    public static CompletionItem getContainerTag() {
        KeyValuePair keyValuePair = new KeyValuePair("tag", "", 1, ValueType.STRING);
        String s = keyValuePair.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("tag");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("tag");
        //item.setSortText("1");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }

    public static CompletionItem getContainerImageBase() {
        KeyValuePair keyValuePair = new KeyValuePair("base", "", 1, ValueType.STRING);
        String s = keyValuePair.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("base");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("base");
        //item.setSortText("1");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }

    public static CompletionItem getContainerImageUserSnippet() {
        Table rootNode = new Table("container.image.user");
        rootNode.addKeyValuePair("run_as", "", ValueType.STRING);
        String s = rootNode.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("container.image.user");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("container.image.user");
        //item.setSortText("1");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }

    public static CompletionItem getProbePortSnippet() {
        KeyValuePair keyValuePair = new KeyValuePair("port", "", 1, ValueType.NUMBER);
        String s = keyValuePair.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("port");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("port");
        //item.setSortText("1");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }

    public static CompletionItem getProbePathSnippet() {
        KeyValuePair keyValuePair = new KeyValuePair("path", "", 1, ValueType.STRING);
        String s = keyValuePair.prettyPrint();
        CompletionItem item = new CompletionItem();
        item.setLabel("path");
        item.setKind(CompletionItemKind.Snippet);
        item.setDetail("path");
        //item.setSortText("1");
        item.setInsertText(s);
        item.setInsertTextFormat(InsertTextFormat.Snippet);

        return item;
    }
}
