package org.ballerinalang.langserver.completions.toml;

import io.ballerina.toml.validator.schema.AbstractSchema;
import io.ballerina.toml.validator.schema.ArraySchema;
import io.ballerina.toml.validator.schema.BooleanSchema;
import io.ballerina.toml.validator.schema.NumericSchema;
import io.ballerina.toml.validator.schema.ObjectSchema;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.toml.validator.schema.SchemaVisitor;
import io.ballerina.toml.validator.schema.StringSchema;
import io.ballerina.toml.validator.schema.Type;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.HashMap;
import java.util.Map;

/**
 * Schema Visitor for Code to Cloud.
 *
 * @since 2.0.0
 */
public class C2CSchemaVisitor extends SchemaVisitor {

    Map<String, Map<String, CompletionItem>> map = new HashMap<>();
    Map<String, CompletionItem> parentStore;
    String parentKey;

    @Override
    public void visit(Schema rootSchema) {
        Map<String, AbstractSchema> properties = rootSchema.properties();
        for (Map.Entry<String, AbstractSchema> entry : properties.entrySet()) {
            this.parentStore = new HashMap<>();
            String key = entry.getKey();
            this.parentKey = key;
            Table objectNode = new Table(key);
            CompletionItem item = new CompletionItem();
            item.setInsertText(objectNode.prettyPrint());
            item.setDetail("Table");
            item.setLabel(this.parentKey);
            AbstractSchema value = entry.getValue();
            this.parentStore.put(this.parentKey, item);
            Map<String, CompletionItem> store = new HashMap<>();
            this.parentStore = store;
            visitNode(value);
            map.put(this.parentKey, store);
        }
    }

    @Override
    public void visit(ObjectSchema objectSchema) {
        Map<String, AbstractSchema> properties = objectSchema.properties();
        Map<String, CompletionItem> completionStore = this.parentStore;
        for (Map.Entry<String, AbstractSchema> entry : properties.entrySet()) {
            CompletionItem item = new CompletionItem();
            String key = entry.getKey();
            AbstractSchema value = entry.getValue();
            if (value.type() == Type.OBJECT) {
//                String oldParentKey = this.parentKey;
                key = this.parentKey + "." + key;
//                this.parentKey = key;
                generateTableSnippet(item, key);
                completionStore.put(key, item);

                Map<String, CompletionItem> store = new HashMap<>();
//                this.parentStore = store;
                visitNode(value, key, store);
                map.put(key, store);
//                this.parentKey = oldParentKey;
            } else if (value.type() == Type.ARRAY) {
                key = this.parentKey + "." + key;
                visitNode(value, key, completionStore);
            } else {
                visitNode(value, key, completionStore);
            }
        }
    }

    private void generateTableSnippet(CompletionItem item, String key) {
        Table objectNode = new Table(key);
        item.setInsertText(objectNode.prettyPrint());
        item.setDetail("Table");
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
    }

    @Override
    public void visit(ArraySchema arraySchema) {
        String key = this.parentKey;
        Map<String, CompletionItem> parentStore = this.parentStore;
        CompletionItem item = generateArraySnippet(key);
        parentStore.put(key, item);

        AbstractSchema items = arraySchema.items();
        Map<String, CompletionItem> store = new HashMap<>();
        visitNode(items, key, store);
        this.map.put(key, store);
    }

    private CompletionItem generateArraySnippet(String key) {
        CompletionItem item = new CompletionItem();
        TableArray objectNode = new TableArray(key);
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail("Table Array");
        return item;
    }

    @Override
    public void visit(BooleanSchema booleanSchema) {
        String key = this.parentKey;
        CompletionItem item = generateBooleanSnippet(key);
        this.parentStore.put(key, item);
    }

    private CompletionItem generateBooleanSnippet(String key) {
        CompletionItem item = new CompletionItem();
        KeyValuePair objectNode = new KeyValuePair(key, ValueType.NUMBER);
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail("Boolean");
        return item;
    }

    @Override
    public void visit(NumericSchema numericSchema) {
        String key = this.parentKey;
        CompletionItem item = generateNumericSnippet(key);
        this.parentStore.put(key, item);
    }

    private CompletionItem generateNumericSnippet(String key) {
        KeyValuePair objectNode = new KeyValuePair(key, ValueType.NUMBER);
        CompletionItem item = new CompletionItem();
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail("Number");
        return item;
    }

    @Override
    public void visit(StringSchema stringSchema) {
        String key = this.parentKey;
        CompletionItem item = generateStringSnippet(key);
        this.parentStore.put(key, item);
    }

    private CompletionItem generateStringSnippet(String key) {
        CompletionItem item = new CompletionItem();
        KeyValuePair objectNode = new KeyValuePair(key, ValueType.STRING);
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail("String");
        return item;
    }

    private void visitNode(AbstractSchema schema) {
        String oldKey = this.parentKey;
        Map<String, CompletionItem> oldStore = this.parentStore;
        schema.accept(this);
        this.parentKey = oldKey;
        this.parentStore = oldStore;
    }

    private void visitNode(AbstractSchema schema, String newKey, Map<String, CompletionItem> newParentStore) {
        String oldKey = this.parentKey;
        Map<String, CompletionItem> oldStore = this.parentStore;
        this.parentKey = newKey;
        this.parentStore = newParentStore;
        schema.accept(this);
        this.parentKey = oldKey;
        this.parentStore = oldStore;
    }

    public Map<String, Map<String, CompletionItem>> getAllCompletionSnippets() {
        return map;
    }
}
