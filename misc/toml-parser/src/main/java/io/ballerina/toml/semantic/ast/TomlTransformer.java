/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.toml.semantic.ast;

import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.diagnostics.DiagnosticLog;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.syntax.tree.ArrayNode;
import io.ballerina.toml.syntax.tree.BoolLiteralNode;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.IdentifierLiteralNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.LiteralStringLiteralNode;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.NodeTransformer;
import io.ballerina.toml.syntax.tree.NumericLiteralNode;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.syntax.tree.ValueNode;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transformer to transform Syntax tree into Abstract Syntax Tree.
 *
 * @since 2.0.0
 */
public class TomlTransformer extends NodeTransformer<TomlNode> {

    private DiagnosticLog dlog;

    public TomlTransformer() {
        this.dlog = DiagnosticLog.getInstance();
    }

    @Override
    public TomlNode transform(DocumentNode documentNode) {
        TomlTableNode rootTable = createRootTable(documentNode);

        NodeList<DocumentMemberDeclarationNode> members = documentNode.members();
        for (DocumentMemberDeclarationNode rootNode : members) {
            TomlNode transformedChild = rootNode.apply(this);
            addChildNodeToParent(rootTable, transformedChild);
        }
        return rootTable;
    }

    private TomlTableNode createRootTable(DocumentNode modulePartNode) {
        TomlKeyEntryNode root = new TomlKeyEntryNode(new TomlUnquotedKeyNode("__root", getPosition(modulePartNode)));
        List<TomlKeyEntryNode> tomlKeyEntryNodes = Collections.singletonList(root);
        TomlKeyNode tomlKeyNode = new TomlKeyNode(tomlKeyEntryNodes, getLocationOfKeyEntryList(tomlKeyEntryNodes));
        return new TomlTableNode(tomlKeyNode, getPosition(modulePartNode));
    }

    private void addChildNodeToParent(TomlTableNode rootTable, TomlNode transformedChild) {
        if (transformedChild.kind() == TomlType.TABLE) {
            TomlTableNode tableChild = (TomlTableNode) transformedChild;
            addChildTableToParent(rootTable, tableChild);
        } else if (transformedChild.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode transformedArray = (TomlTableArrayNode) transformedChild;
            addChildParentArrayToParent(rootTable, transformedArray);
        } else if (transformedChild.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode transformedKeyValuePair = (TomlKeyValueNode) transformedChild;
            addChildKeyValueToParent(rootTable, transformedKeyValuePair);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void addChildKeyValueToParent(TomlTableNode rootTable, TomlKeyValueNode transformedKeyValuePair) {
        List<TomlKeyEntryNode> keys = transformedKeyValuePair.key().keys();
        List<String> parentTables = new ArrayList<>();
        for (int i = 0; i < keys.size() - 1; i++) {
            parentTables.add(keys.get(i).name().toString());
        }
        TomlTableNode parentTable = rootTable;
        for (int i = 0; i < parentTables.size(); i++) {
            String newTable = parentTables.get(i);
            TopLevelNode dottedParentNode = parentTable.entries().get(newTable);
            if (dottedParentNode != null) {
                if (dottedParentNode.kind() == TomlType.TABLE) {
                    parentTable = (TomlTableNode) dottedParentNode;
                } else {
                    TomlDiagnostic nodeExists =
                            dlog.error(dottedParentNode.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE, newTable);
                    parentTable.addDiagnostic(nodeExists);
                }
            } else {
                //create the table
                TomlKeyEntryNode tomlKeyEntryNode = keys.get(i);
                parentTable = createDottedKeyParentTable(parentTable, tomlKeyEntryNode);
            }
        }
        if (isDottedKey(keys)) {
            List<TomlKeyEntryNode> list = new ArrayList<>();
            list.add(keys.get(keys.size() - 1));
            TomlKeyNode newKey = new TomlKeyNode(list, getLocationOfKeyEntryList(list));
            transformedKeyValuePair = new TomlKeyValueNode(newKey, transformedKeyValuePair.value(),
                    transformedKeyValuePair.location());
        }
        addChildToTableAST(parentTable, transformedKeyValuePair);
    }

    private void addChildToTableAST(TomlTableNode parentTable, TopLevelNode value) {
        Map<String, TopLevelNode> entries = parentTable.entries();
        String key = value.key().name();
        if (entries.containsKey(key)) {
            TomlDiagnostic nodeExists = dlog.error(value.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE, key);
            parentTable.addDiagnostic(nodeExists);
        } else {
            entries.put(key, value);
        }
    }

    private TomlTableNode createDottedKeyParentTable(TomlTableNode parentTable, TomlKeyEntryNode dottedKey) {
        List<TomlKeyEntryNode> list = new ArrayList<>();
        list.add(dottedKey);
        TomlKeyNode newTableKey = new TomlKeyNode(list, getLocationOfKeyEntryList(list));
        TomlTableNode newTomlTableNode = new TomlTableNode(newTableKey, null);
        addChildToTableAST(parentTable, newTomlTableNode);
        return newTomlTableNode;
    }

    private void addChildParentArrayToParent(TomlTableNode rootTable, TomlTableArrayNode tableArrayChild) {
        TomlTableNode parentTable = getParentTable(rootTable, tableArrayChild);
        List<TomlKeyEntryNode> keys = tableArrayChild.key().keys();
        TomlKeyEntryNode tomlKeyEntryNode = keys.get(keys.size() - 1);
        List<TomlKeyEntryNode> list = new ArrayList<>();
        list.add(tomlKeyEntryNode);

        TomlTableArrayNode newTomlTableArray = new TomlTableArrayNode(new TomlKeyNode(list,
                getLocationOfKeyEntryList(list)), tableArrayChild.location(), tableArrayChild.children());
        TopLevelNode topLevelNode = parentTable.entries().get(newTomlTableArray.key().name());
        if (topLevelNode == null) {
            addChildToTableAST(parentTable, newTomlTableArray);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTableArrayNode) {
                ((TomlTableArrayNode) topLevelNode).addChild(newTomlTableArray.children().get(0));
            } else if (topLevelNode instanceof TomlKeyValueNode) {
                TomlDiagnostic nodeExists = dlog.error(newTomlTableArray.location(),
                        DiagnosticErrorCode.ERROR_EXISTING_NODE, newTomlTableArray.key().name());
                parentTable.addDiagnostic(nodeExists);

            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private TomlKeyEntryNode getLastKeyEntry(TopLevelNode childNode) {
        return childNode.key().keys().get(childNode.key().keys().size() - 1);
    }

    private TomlTableNode getParentTable(TomlTableNode rootTable, TopLevelNode childNode) {
        List<String> parentTables = new ArrayList<>();
        for (int i = 0; i < (childNode.key().keys().size() - 1); i++) {
            parentTables.add(childNode.key().keys().get(i).name().toString());
        }

        TomlTableNode parentTable = rootTable;
        for (int i = 0; i < parentTables.size(); i++) {
            String parentString = parentTables.get(i);
            TopLevelNode rootTableNode = parentTable.entries().get(parentString);
            if (rootTableNode != null) {
                parentTable = (TomlTableNode) rootTableNode;
            } else {
                TomlKeyEntryNode tomlKeyEntryNode = childNode.key().keys().get(i);
                if (childNode instanceof TomlTableArrayNode) {
                    parentTable = generateTable(parentTable, tomlKeyEntryNode, false);
                } else {
                    parentTable = generateTable(parentTable, tomlKeyEntryNode, true);
                }
            }
        }
        return parentTable;
    }

    private void addChildTableToParent(TomlTableNode rootTable, TomlTableNode tableChild) {
        TomlTableNode parentTable = getParentTable(rootTable, tableChild);
        TopLevelNode topLevelNode = parentTable.entries().get(tableChild.key().name());
        TomlKeyEntryNode lastKeyEntry = getLastKeyEntry(tableChild);
        List<TomlKeyEntryNode> entries = new ArrayList<>();
        entries.add(lastKeyEntry);
        TomlTableNode newTableNode = new TomlTableNode(new TomlKeyNode(entries,getLocationOfKeyEntryList(entries)),
                tableChild.generated(), tableChild.location(), tableChild.entries());
        if (topLevelNode == null) {
            addChildToTableAST(parentTable, newTableNode);
        } else {
            if (topLevelNode instanceof TomlTableNode) {
                TomlTableNode targetTable = (TomlTableNode) topLevelNode;
                if ((targetTable).generated()) {
                    parentTable.replaceGeneratedTable(newTableNode);
                } else {
                    TomlDiagnostic nodeExists = dlog.error(tableChild.location(),
                            DiagnosticErrorCode.ERROR_EXISTING_NODE, newTableNode.key().name());
                    parentTable.addDiagnostic(nodeExists);
                }
            } else if (topLevelNode instanceof TomlKeyValueNode) {
                TomlDiagnostic nodeExist = dlog.error(newTableNode.location(),
                        DiagnosticErrorCode.ERROR_EXISTING_NODE, tableChild.key().name());
                parentTable.addDiagnostic(nodeExist);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private TomlTableNode generateTable(TomlTableNode parentTable, TomlKeyEntryNode parentString, boolean isGenerated) {
        List<TomlKeyEntryNode> list = new ArrayList<>();
        list.add(parentString);
        TomlKeyNode newTableKey = new TomlKeyNode(list,getLocationOfKeyEntryList(list));
        TomlTableNode newTomlTableNode = new TomlTableNode(newTableKey, isGenerated, null);
        addChildToTableAST(parentTable, newTomlTableNode);
        return newTomlTableNode;
    }

    @Override
    public TomlNode transform(TableNode tableNode) {
        SeparatedNodeList<ValueNode> identifierList = tableNode.identifier();
        TomlKeyNode tomlKeyNode = getTomlKeyNode(identifierList);
        TomlTableNode tomlTableNode = new TomlTableNode(tomlKeyNode, getPosition(tableNode));
        addChildToTable(tableNode, tomlTableNode);
        return tomlTableNode;
    }

    private void addChildToTable(TableNode stTableNode, TomlTableNode astTomlTableNode) {
        NodeList<KeyValueNode> children = stTableNode.fields();
        for (KeyValueNode child : children) {
            TomlNode transformedChild = child.apply(this);
            if (transformedChild instanceof TomlKeyValueNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
                checkExistingNodes(astTomlTableNode, topLevelChild);
                addChildKeyValueToParent(astTomlTableNode, (TomlKeyValueNode) transformedChild);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private boolean isDottedKey(List<TomlKeyEntryNode> keys) {
        return keys.size() > 1;
    }

    private void checkExistingNodes(TomlTableNode tomlTableNode, TopLevelNode topLevelChild) {
        Map<String, TopLevelNode> childs = tomlTableNode.entries();
        String childName = topLevelChild.key().name();
        if (childs.get(childName) != null) {
            TomlDiagnostic nodeExists = dlog.error(topLevelChild.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE,
                    childName);
            tomlTableNode.addDiagnostic(nodeExists);
        }
    }

    @Override
    public TomlNode transform(TableArrayNode tableArrayNode) {
        SeparatedNodeList<ValueNode> identifierList = tableArrayNode.identifier();
        TomlKeyNode tomlKeyNode = getTomlKeyNode(identifierList);
        TomlTableArrayNode tomlTableArrayNode = new TomlTableArrayNode(tomlKeyNode, getPosition(tableArrayNode));
        TomlTableNode anonTable = addChildsToTableArray(tableArrayNode);
        tomlTableArrayNode.addChild(anonTable); //As Syntax tree follows a flat hierarchy and contains only one table
        return tomlTableArrayNode;
    }

    private TomlTableNode addChildsToTableArray(TableArrayNode tableArrayNode) {
        NodeList<KeyValueNode> children = tableArrayNode.fields();
        TomlNodeLocation position = getPosition(tableArrayNode);
        TomlKeyNode anonKey = new TomlKeyNode(null, null);
        TomlTableNode anonTable = new TomlTableNode(anonKey, position);
        for (KeyValueNode child : children) {
            TomlNode transformedChild = child.apply(this);
            if (transformedChild instanceof TomlKeyValueNode) {
                addChildKeyValueToParent(anonTable, (TomlKeyValueNode) transformedChild);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return anonTable;
    }

    @Override
    public TomlNode transform(KeyValueNode keyValue) {
        SeparatedNodeList<ValueNode> identifierList = keyValue.identifier();

        TomlKeyNode tomlKeyNode = getTomlKeyNode(identifierList);
        ValueNode value = keyValue.value();
        TomlValueNode tomlValue = transformValue(value);

        return new TomlKeyValueNode(tomlKeyNode, tomlValue, getPosition(keyValue));
    }

    private TomlKeyNode getTomlKeyNode(SeparatedNodeList<ValueNode> identifierList) {
        List<TomlKeyEntryNode> nodeList = new ArrayList<>();
        for (Node node : identifierList) {
            TomlBasicValueNode transformedNode = (TomlBasicValueNode) node.apply(this);
            nodeList.add(new TomlKeyEntryNode(transformedNode));
        }

        return new TomlKeyNode(nodeList, getLocationOfKeyEntryList(nodeList));
    }

    private TomlValueNode transformValue(ValueNode valueToken) {
        return (TomlValueNode) valueToken.apply(this);
    }

    @Override
    public TomlNode transform(ArrayNode array) {
        SeparatedNodeList<ValueNode> values = array.values();
        List<TomlValueNode> elements = new ArrayList<>();
        for (ValueNode value : values) {
            TomlValueNode transformedValue = (TomlValueNode) value.apply(this);
            elements.add(transformedValue);
        }
        return new TomlArrayValueNode(elements, getPosition(array));
    }

    @Override
    protected TomlNode transformSyntaxNode(Node node) {
        return null;
    }

    private TomlNodeLocation getPosition(Node node) {
        return new TomlNodeLocation(node.lineRange(), node.textRange());
    }

    /**
     * Transforms ST StringLiteralNode into AST string node.
     *
     * @param stringLiteralNode Syntax Tree representative for string nodes
     * @return AST string value representative node
     */
    @Override
    public TomlNode transform(StringLiteralNode stringLiteralNode) {
        boolean multilineString = isMultilineString(stringLiteralNode.startDoubleQuote());
        Optional<Token> content = stringLiteralNode.content();
        String valueString;
        if (content.isEmpty()) {
            valueString = "";
        } else {
            valueString = content.get().text();
            if (multilineString) {
                valueString = removeFirstNewline(valueString);
                valueString = trimBackslashWhitespaces(valueString);
            }
        }
        String unescapedJava = StringEscapeUtils.unescapeJava(valueString);
        TomlNodeLocation position = getPosition(stringLiteralNode);

        return new TomlStringValueNode(unescapedJava, position);
    }

    private String trimBackslashWhitespaces(String value) {
        StringBuilder output = new StringBuilder();
        String[] split = value.split("\\\\\\r?\\n");
        for (String str : split) {
            output.append(str.stripLeading());
        }
        return output.toString();
    }

    /**
     * Transforms ST LiteralStringLiteralNode into AST literal string node.
     *
     * @param literalStringLiteralNode Syntax Tree representative for literal string nodes
     * @return AST Literal string value representative node
     */
    @Override
    public TomlNode transform(LiteralStringLiteralNode literalStringLiteralNode) {
        boolean multilineString = isMultilineString(literalStringLiteralNode.startSingleQuote());
        Optional<Token> content = literalStringLiteralNode.content();
        String valueString;
        if (content.isEmpty()) {
            valueString = "";
        } else {
            valueString = content.get().text();
            if (multilineString) {
                valueString = removeFirstNewline(valueString);
            }
        }
        TomlNodeLocation position = getPosition(literalStringLiteralNode);

        return new TomlStringValueNode(valueString, position);
    }

    private boolean isMultilineString(Token token) {
        return token.kind() == SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN ||
                token.kind() == SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN;
    }

    private String removeFirstNewline(String value) {
        if (value.startsWith("\n")) {
            return value.substring(1);
        }
        if (value.startsWith("\r\n")) {
            return value.substring(2);
        }
        return value;
    }

    /**
     * Transforms ST NumericLiteralNode into AST numerical node.
     *
     * @param numericLiteralNode Syntax Tree representative for numerical nodes
     * @return AST Numerical Value representative node
     */
    @Override
    public TomlNode transform(NumericLiteralNode numericLiteralNode) {
        String sign = "";
        if (numericLiteralNode.sign().isPresent()) {
            sign = numericLiteralNode.sign().get().text();
        }
        Token valueToken = numericLiteralNode.value();
        return getTomlNode(numericLiteralNode, sign + valueToken.text());
    }

    private TomlNode getTomlNode(NumericLiteralNode numericLiteralNode, String value) {
        value = value.replace("_", "");
        if (numericLiteralNode.kind() == SyntaxKind.DEC_INT) {
            return new TomlLongValueNode(Long.parseLong(value),
                    getPosition(numericLiteralNode));
        } else if (numericLiteralNode.kind() == SyntaxKind.HEX_INT) {
            value = value.replace("0x", "").replace("0X", "");
            return new TomlLongValueNode(Long.parseLong(value, 16),
                    getPosition(numericLiteralNode));
        } else if (numericLiteralNode.kind() == SyntaxKind.OCT_INT) {
            value = value.replace("0o", "").replace("0O", "");
            return new TomlLongValueNode(Long.parseLong(value, 8),
                    getPosition(numericLiteralNode));
        } else if (numericLiteralNode.kind() == SyntaxKind.BINARY_INT) {
            value = value.replace("0b", "").replace("0B", "");
            return new TomlLongValueNode(Long.parseLong(value, 2),
                    getPosition(numericLiteralNode));
        } else {
            return new TomlDoubleValueNodeNode(Double.parseDouble(value),
                    getPosition(numericLiteralNode));
        }
    }

    /**
     * Transforms ST BoolLiteralNode into AST TomlBooleanValue Node.
     *
     * @param boolLiteralNode Syntax Tree representative for boolean
     * @return AST Boolean Value representative node
     */
    @Override
    public TomlNode transform(BoolLiteralNode boolLiteralNode) {
        if (boolLiteralNode.value().kind() == SyntaxKind.TRUE_KEYWORD) {
            return new TomlBooleanValueNode(true, getPosition(boolLiteralNode));
        } else {
            return new TomlBooleanValueNode(false, getPosition(boolLiteralNode));
        }
    }

    @Override
    public TomlNode transform(IdentifierLiteralNode identifierLiteralNode) {
        return new TomlUnquotedKeyNode(identifierLiteralNode.value().text(), getPosition(identifierLiteralNode));
    }

    private TomlNodeLocation getLocationOfKeyEntryList(List<TomlKeyEntryNode> keys) {
        if (keys.size() == 0) {
            return null;
        }
        int startOffset = keys.get(0).location().textRange().startOffset();
        int length = 0;
        for (TomlKeyEntryNode entryNode : keys) {
            length += entryNode.location().textRange().length() + 1;
        }
        TextRange textRange = TextRange.from(startOffset, length - 1);
        LineRange lineRange = LineRange
                .from(keys.get(0).location().lineRange().filePath(), keys.get(0).location().lineRange().startLine(),
                        keys.get(keys.size() - 1).location().lineRange().endLine());
        return new TomlNodeLocation(lineRange, textRange);
    }
}
