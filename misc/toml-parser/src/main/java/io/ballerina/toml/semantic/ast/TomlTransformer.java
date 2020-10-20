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
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.NodeTransformer;
import io.ballerina.toml.syntax.tree.NumericLiteralNode;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.ValueNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public TomlNode transform(DocumentNode modulePartNode) {
        TomlTableNode rootTable = createRootTable();

        NodeList<DocumentMemberDeclarationNode> members = modulePartNode.members();
        for (DocumentMemberDeclarationNode rootNode : members) {
            TomlNode transformedChild = rootNode.apply(this);
            addChildNodeToParent(rootTable, transformedChild);
        }
        return rootTable;
    }

    private TomlTableNode createRootTable() {
        TomlKeyNode tomlKeyNode = new TomlKeyNode(null);
        return new TomlTableNode(tomlKeyNode, null);
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
            TopLevelNode dottedParentNode = parentTable.children().get(newTable);
            if (dottedParentNode != null) {
                //TOOD fix
            } else {
                //create the table
                TomlKeyEntryNode tomlKeyEntryNode = keys.get(i);
                parentTable = createDottedKeyParentTable(parentTable, tomlKeyEntryNode);
            }
        }
        if (keys.size() > 1) {
            List<TomlKeyEntryNode> list = new ArrayList<>();
            list.add(keys.get(keys.size() - 1));
            TomlKeyNode newKey = new TomlKeyNode(list);
            transformedKeyValuePair = new TomlKeyValueNode(newKey, transformedKeyValuePair.value(),
                    transformedKeyValuePair.location());
        }
        parentTable.children().put(transformedKeyValuePair.key().name(), transformedKeyValuePair);
    }

    private TomlTableNode createDottedKeyParentTable(TomlTableNode parentTable, TomlKeyEntryNode dottedKey) {
        List<TomlKeyEntryNode> list = new ArrayList<>();
        list.add(dottedKey);
        TomlKeyNode newTableKey = new TomlKeyNode(list);
        TomlTableNode newTomlTableNode = new TomlTableNode(newTableKey, null);
        parentTable.children().put(dottedKey.name().toString(), newTomlTableNode);
        return newTomlTableNode;
    }

    private void addChildParentArrayToParent(TomlTableNode rootTable, TomlTableArrayNode tableArrayChild) {
        TomlTableNode parentTable = getParentTable(rootTable, tableArrayChild);
        List<TomlKeyEntryNode> keys = tableArrayChild.key().keys();
        TomlKeyEntryNode tomlKeyEntryNode = keys.get(keys.size() - 1);
        List<TomlKeyEntryNode> list = new ArrayList<>();
        list.add(tomlKeyEntryNode);

        TomlTableArrayNode newTomlTableArray = new TomlTableArrayNode(new TomlKeyNode(list),
                tableArrayChild.location(), tableArrayChild.children());
        TopLevelNode topLevelNode = parentTable.children().get(newTomlTableArray.key().name());
        if (topLevelNode == null) {
            parentTable.children().put(newTomlTableArray.key().name(), newTomlTableArray);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTableArrayNode) {
                ((TomlTableArrayNode) topLevelNode).addChild(newTomlTableArray.children().get(0));
            } else if (topLevelNode instanceof TomlKeyValueNode) {
                TomlDiagnostic nodeExists =
                        dlog.error(newTomlTableArray.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE);
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
        String tableLeadName = getLastKeyEntry(childNode).name().toString();
        List<String> parentTables = new ArrayList<>();
        for (int i = 0; i < (childNode.key().keys().size() - 1); i++) {
            parentTables.add(childNode.key().keys().get(i).name().toString());
        }

        TomlTableNode parentTable = rootTable;
        for (int i = 0; i < parentTables.size(); i++) {
            String parentString = parentTables.get(i);
            TopLevelNode rootTableNode = parentTable.children().get(parentString);
            if (rootTableNode != null) {
                parentTable = (TomlTableNode) rootTableNode;
            } else {
                TomlKeyEntryNode tomlKeyEntryNode = childNode.key().keys().get(i);
                if (childNode instanceof TomlTableArrayNode) {
                    parentTable = generateTable(parentTable.children(), tomlKeyEntryNode, false);
                } else {
                    parentTable = generateTable(parentTable.children(), tomlKeyEntryNode, true);
                }
            }
        }

        TopLevelNode lastNode = parentTable.children().get(tableLeadName);
        if (lastNode instanceof TomlKeyValueNode) {
            TomlDiagnostic nodeExists =
                    dlog.error(childNode.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE);
            //TODO revisit dotted.
            parentTable.addDiagnostic(nodeExists);
        }
        return parentTable;
    }

    private void addChildTableToParent(TomlTableNode rootTable, TomlTableNode tableChild) {
        TomlTableNode parentTable = getParentTable(rootTable, tableChild);
        TopLevelNode topLevelNode = parentTable.children().get(tableChild.key().name());
        TomlKeyEntryNode lastKeyEntry = getLastKeyEntry(tableChild);
        List<TomlKeyEntryNode> entries = new ArrayList<>();
        entries.add(lastKeyEntry);
        TomlTableNode newTableNode = new TomlTableNode(new TomlKeyNode(entries),
                tableChild.generated(), tableChild.location(), tableChild.children());
        if (topLevelNode == null) {
            parentTable.children().put(newTableNode.key().name(), newTableNode);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTableNode) {
                if (((TomlTableNode) topLevelNode).generated()) {
                    parentTable.replaceGeneratedTable(newTableNode);
                } else {
                    TomlDiagnostic nodeExist = dlog.error(newTableNode.location(),
                            DiagnosticErrorCode.ERROR_EXISTING_NODE);
                    parentTable.addDiagnostic(nodeExist);
                }
            } else if (topLevelNode instanceof TomlKeyValueNode) {
                TomlDiagnostic nodeExist = dlog.error(newTableNode.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE);
                parentTable.addDiagnostic(nodeExist);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private TomlTableNode generateTable(Map<String, TopLevelNode> childs, TomlKeyEntryNode parentString,
                                        boolean isGenerated) {
        List<TomlKeyEntryNode> list = new ArrayList<>();
        list.add(parentString);
        TomlKeyNode newTableKey = new TomlKeyNode(list); //TODO Revisit
        TomlTableNode newTomlTableNode = new TomlTableNode(newTableKey, isGenerated, null);
        childs.put(parentString.name().toString(), newTomlTableNode);
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

    private void addChildToTable(TableNode tableNode, TomlTableNode tomlTableNode) {
        NodeList<KeyValueNode> children = tableNode.fields();
        for (KeyValueNode child : children) {
            TomlNode transformedChild = child.apply(this); //todo dotted
            if (transformedChild instanceof TopLevelNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
                checkExistingNodes(tomlTableNode, topLevelChild);
                tomlTableNode.children().put(topLevelChild.key().name(), topLevelChild);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void checkExistingNodes(TomlTableNode tomlTableNode, TopLevelNode topLevelChild) {
        Map<String, TopLevelNode> childs = tomlTableNode.children();
        String childName = topLevelChild.key().name();
        if (childs.get(childName) != null) {
            TomlDiagnostic nodeExists = dlog.error(topLevelChild.location(), DiagnosticErrorCode.ERROR_EXISTING_NODE);
            tomlTableNode.addDiagnostic(nodeExists);
        }
    }

    @Override
    public TomlNode transform(TableArrayNode tableArrayNode) {
        SeparatedNodeList<ValueNode> identifierList = tableArrayNode.identifier();
        TomlKeyNode tomlKeyNode = getTomlKeyNode(identifierList);
        TomlTableArrayNode tomlTableArrayNode = new TomlTableArrayNode(tomlKeyNode, getPosition(tableArrayNode));
        TomlTableNode anonTable = addChildsToTableArray(tableArrayNode);
        tomlTableArrayNode.addChild(anonTable);
        return tomlTableArrayNode;
    }

    private TomlTableNode addChildsToTableArray(TableArrayNode tableArrayNode) {
        NodeList<KeyValueNode> children = tableArrayNode.fields();
        TomlNodeLocation position = getPosition(tableArrayNode);
        TomlKeyNode anonKey = new TomlKeyNode(null);
        TomlTableNode anonTable = new TomlTableNode(anonKey, position);
        for (KeyValueNode child : children) {
            TomlNode transformedChild = child.apply(this); //todo dotted
            if (transformedChild instanceof TopLevelNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
//                checkExistingNodes(tomlTableArray,topLevelChild);
                anonTable.children().put(topLevelChild.key().name(), topLevelChild);
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

    private TomlKeyNode getTomlKeyNode(SeparatedNodeList<ValueNode> identifierList) { //Refactor
        List<TomlKeyEntryNode> nodeList = new ArrayList<>();
        for (Node node : identifierList) {
            nodeList.add(new TomlKeyEntryNode ((TomlBasicValueNode) node.apply(this)));
        }

        return new TomlKeyNode(nodeList);
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

    @Override
    public TomlNode transform(StringLiteralNode stringLiteralNode) {
        String valueString = stringLiteralNode.content().text();
        TomlNodeLocation position = getPosition(stringLiteralNode);

        return new TomlStringValueNode(valueString, position);
    }

    @Override
    public TomlNode transform(NumericLiteralNode numericLiteralNode) {
        if (numericLiteralNode.kind() == SyntaxKind.DEC_INT) {
            return new TomlLongValueNode(Long.parseLong(numericLiteralNode.value().text()),
                    getPosition(numericLiteralNode));
        } else {
            return new TomlDoubleValueNodeNode(Double.parseDouble(numericLiteralNode.value().text()),
                    getPosition(numericLiteralNode));
        }
    }

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
}
