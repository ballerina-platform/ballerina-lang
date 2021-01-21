/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Schema Visitor for Toml based Json Schema.
 *
 * @since 2.0.0
 */
public class TomlSchemaVisitor extends SchemaVisitor {

    private final Map<Parent, Map<String, CompletionItem>> completions = new HashMap<>();
    private Map<String, CompletionItem> parentStore;
    private Parent parentKey;

    @Override
    public void visit(Schema rootSchema) {
        Map<String, AbstractSchema> properties = rootSchema.properties();
        for (Map.Entry<String, AbstractSchema> entry : properties.entrySet()) {
            this.parentStore = new HashMap<>();
            String key = entry.getKey();
            this.parentKey = new Parent(key, ParentType.TABLE);
            CompletionItem item = generateRootTableCompletionItem(key);
            AbstractSchema value = entry.getValue();
            this.parentStore.put(key, item);
            Map<String, CompletionItem> store = new HashMap<>();
            this.parentStore = store;
            visitNode(value);
            completions.put(this.parentKey, store);
        }
    }

    @Override
    public void visit(ObjectSchema objectSchema) {
        Map<String, AbstractSchema> properties = objectSchema.properties();
        Map<String, CompletionItem> completionStore = this.parentStore;
        for (Map.Entry<String, AbstractSchema> entry : properties.entrySet()) {
            String key = entry.getKey();
            AbstractSchema value = entry.getValue();
            if (value.type() == Type.OBJECT) {
                key = this.parentKey.getKey() + "." + key;
                CompletionItem item = generateTableSnippet(key);
                completionStore.put(key, item);
                Map<String, CompletionItem> store = new HashMap<>();
                visitNode(value, new Parent(key, ParentType.TABLE), store);
                completions.put(new Parent(key, ParentType.TABLE), store);
            } else if (value.type() == Type.ARRAY) {
                key = this.parentKey.getKey() + "." + key;
                visitNode(value, new Parent(key, ParentType.TABLE), completionStore);
            } else {
                visitNode(value, new Parent(key, ParentType.TABLE), completionStore);
            }
        }
    }

    @Override
    public void visit(ArraySchema arraySchema) {
        String key = this.parentKey.getKey();
        Map<String, CompletionItem> parentStore = this.parentStore;
        CompletionItem item = generateArraySnippet(key);
        parentStore.put(key, item);

        AbstractSchema items = arraySchema.items();
        Map<String, CompletionItem> store = new HashMap<>();
        visitNode(items, new Parent(key, ParentType.TABLE_ARRAY), store);
        this.completions.put(new Parent(key, ParentType.TABLE_ARRAY), store);
    }

    @Override
    public void visit(BooleanSchema booleanSchema) {
        String key = this.parentKey.getKey();
        CompletionItem item = generateBooleanSnippet(key);
        this.parentStore.put(key, item);
    }

    @Override
    public void visit(NumericSchema numericSchema) {
        String key = this.parentKey.getKey();
        CompletionItem item = generateNumericSnippet(key);
        this.parentStore.put(key, item);
    }

    @Override
    public void visit(StringSchema stringSchema) {
        String key = this.parentKey.getKey();
        CompletionItem item = generateStringSnippet(key);
        this.parentStore.put(key, item);
    }

    private void visitNode(AbstractSchema schema) {
        Parent oldKey = this.parentKey;
        Map<String, CompletionItem> oldStore = this.parentStore;
        schema.accept(this);
        this.parentKey = oldKey;
        this.parentStore = oldStore;
    }

    private void visitNode(AbstractSchema schema, Parent newKey, Map<String, CompletionItem> newParentStore) {
        Parent oldKey = this.parentKey;
        Map<String, CompletionItem> oldStore = this.parentStore;
        this.parentKey = newKey;
        this.parentStore = newParentStore;
        schema.accept(this);
        this.parentKey = oldKey;
        this.parentStore = oldStore;
    }

    public Map<Parent, Map<String, CompletionItem>> getAllCompletionSnippets() {
        return Collections.unmodifiableMap(completions);
    }

    private CompletionItem generateRootTableCompletionItem(String key) {
        Table objectNode = new Table(key);
        CompletionItem item = new CompletionItem();
        item.setInsertText(objectNode.prettyPrint());
        item.setDetail("Table");
        item.setLabel(this.parentKey.getKey());
        item.setSortText(SortingUtil.genSortText(2));
        return item;
    }

    private CompletionItem generateTableSnippet(String key) {
        Table objectNode = new Table(key);
        CompletionItem item = new CompletionItem();
        item.setInsertText(objectNode.prettyPrint());
        item.setDetail(TomlSyntaxTreeUtil.TABLE);
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setSortText(SortingUtil.genSortText(2));
        return item;
    }

    private CompletionItem generateArraySnippet(String key) {
        CompletionItem item = new CompletionItem();
        TableArray objectNode = new TableArray(key);
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(TomlSyntaxTreeUtil.TABLE_ARRAY);
        item.setSortText(SortingUtil.genSortText(2));
        return item;
    }

    private CompletionItem generateBooleanSnippet(String key) {
        CompletionItem item = new CompletionItem();
        KeyValuePair objectNode = new KeyValuePair(key, ValueType.NUMBER);
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(TomlSyntaxTreeUtil.BOOLEAN);
        item.setSortText(SortingUtil.genSortText(1));
        return item;
    }

    private CompletionItem generateNumericSnippet(String key) {
        KeyValuePair objectNode = new KeyValuePair(key, ValueType.NUMBER);
        CompletionItem item = new CompletionItem();
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(TomlSyntaxTreeUtil.NUMBER);
        item.setSortText(SortingUtil.genSortText(1));
        return item;
    }

    private CompletionItem generateStringSnippet(String key) {
        CompletionItem item = new CompletionItem();
        KeyValuePair objectNode = new KeyValuePair(key, ValueType.STRING);
        item.setInsertText(objectNode.prettyPrint());
        item.setLabel(key);
        item.setKind(CompletionItemKind.Snippet);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(TomlSyntaxTreeUtil.STRING);
        item.setSortText(SortingUtil.genSortText(1));
        return item;
    }
}
