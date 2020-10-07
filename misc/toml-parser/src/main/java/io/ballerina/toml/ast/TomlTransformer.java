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

package io.ballerina.toml.ast;

import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.syntax.tree.Array;
import io.ballerina.toml.syntax.tree.BasicValueNode;
import io.ballerina.toml.syntax.tree.KeyValue;
import io.ballerina.toml.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.ModulePartNode;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.NodeTransformer;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.syntax.tree.ValueNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Transformer to transform Syntax tree into Abstract Syntax Tree.
 *
 * @since 0.1.0
 */
public class TomlTransformer extends NodeTransformer<TomlNode> {

    private DiagnosticLog dlog;

    public TomlTransformer() {
        this.dlog = DiagnosticLog.getInstance();
    }

    @Override
    public TomlNode transform(ModulePartNode modulePartNode) {
        TomlTableNode rootTable = createRootTable();

        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (ModuleMemberDeclarationNode rootNode : members) {
            TomlNode transformedChild = rootNode.apply(this);
            addChildNodeToParent(rootTable, transformedChild);
        }
        return rootTable;
    }

    private TomlTableNode createRootTable() {
        TomlKeyNode tomlKeyNode = new TomlKeyNode("RootNode", SyntaxKind.MODULE_NAME, null);
        return new TomlTableNode(tomlKeyNode, null);
    }

    private void addChildNodeToParent(TomlTableNode rootTable, TomlNode transformedChild) {
        if (transformedChild.kind() == SyntaxKind.TABLE) {
            TomlTableNode tableChild = (TomlTableNode) transformedChild;
            addChildTableToParent(rootTable, tableChild);
        } else if (transformedChild.kind() == SyntaxKind.TABLE_ARRAY) {
            TomlTableArrayNode transformedArray = (TomlTableArrayNode) transformedChild;
            addChildParentArrayToParent(rootTable, transformedArray);
        } else if (transformedChild.kind() == SyntaxKind.KEY_VALUE) {
            TomlKeyValueNode transformedKeyValuePair = (TomlKeyValueNode) transformedChild;
            addChildKeyValueToParent(rootTable, transformedKeyValuePair);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void addChildKeyValueToParent(TomlTableNode rootTable, TomlKeyValueNode transformedKeyValuePair) {
        String key = transformedKeyValuePair.key().name();
        String[] split = splitDottedKey(key);
        String[] parentTables = getParentTables(split);
        TomlTableNode parentTable = rootTable;
        for (String newTable : parentTables) {
            TopLevelNode dottedParentNode = parentTable.children().get(newTable);
            if (dottedParentNode != null) {
                //TOOD fix
            } else {
                //create the table
                parentTable = createDottedKeyParentTable(parentTable, newTable);
            }
        }
        if (split.length > 1) {
            TomlKeyNode newKey = new TomlKeyNode(split[split.length - 1], SyntaxKind.STRING_LITERAL,
                    transformedKeyValuePair.location());
            transformedKeyValuePair = new TomlKeyValueNode(newKey, transformedKeyValuePair.value(),
                    transformedKeyValuePair.location());
        }
        parentTable.addChild(transformedKeyValuePair);
    }

    private String createDottedKeyFromList(List<String> parentKeys) {
        return String.join(".", parentKeys);
    }

    private TomlTableNode createDottedKeyParentTable(TomlTableNode parentTable, String dottedKey) { //TODO revisit
        TomlKeyNode newTableKey = new TomlKeyNode(dottedKey, SyntaxKind.STRING_LITERAL, null);
        TomlTableNode newTomlTableNode = new TomlTableNode(newTableKey, null);
        parentTable.children().put(dottedKey, newTomlTableNode);
        return newTomlTableNode;
    }

    private String[] getParentTables(String[] split) {
        return Arrays.copyOf(split, split.length - 1);
    }

    private String[] splitDottedKey(String key) {
        return key.split("\\.");
    }

    private void addChildParentArrayToParent(TomlTableNode rootTable, TomlTableArrayNode tableArrayChild) {
        TomlTableNode parentTable = getParentTable(rootTable, tableArrayChild);
        String[] split = tableArrayChild.key().name().split("\\.");
        String name = split[split.length - 1];
        TomlTableArrayNode newTomlTableArray = new TomlTableArrayNode(new TomlKeyNode(name, tableArrayChild.kind(),
                tableArrayChild.location()), tableArrayChild.location(), tableArrayChild.children());
        TopLevelNode topLevelNode = parentTable.children().get(newTomlTableArray.key().name());
        if (topLevelNode == null) {
            parentTable.addChild(newTomlTableArray);
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

    private TomlTableNode getParentTable(TomlTableNode rootTable, TopLevelNode childNode) {
        String identifierName = childNode.key().name();
        String[] split = splitDottedKey(identifierName);
        String tableLeadName = getLastKeyOfDottedKey(split);
        String[] parentTables = getParentTables(split);

        TomlTableNode parentTable = rootTable;
        List<String> parentTablesString = new ArrayList<>();
        for (String parentString : parentTables) {
            parentTablesString.add(parentString);
            String dottedParentString = createDottedKeyFromList(parentTablesString);
            TopLevelNode rootTableNode = parentTable.children().get(dottedParentString);
            if (rootTableNode != null) {
                parentTable = (TomlTableNode) rootTableNode;
            } else {
                if (childNode instanceof TomlTableArrayNode) {
                    parentTable = generateTable(parentTable.children(), dottedParentString, false);
                } else {
                    parentTable = generateTable(parentTable.children(), dottedParentString, true);
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

    private String getLastKeyOfDottedKey(String[] split) {
        return split[split.length - 1];
    }

    private void addChildTableToParent(TomlTableNode rootTable, TomlTableNode tableChild) {
        TomlTableNode parentTable = getParentTable(rootTable, tableChild);
        TopLevelNode topLevelNode = parentTable.children().get(tableChild.key().name());
        String[] split = tableChild.key().name().split("\\.");
        String name = split[split.length - 1];
        TomlTableNode newTableNode = new TomlTableNode(new TomlKeyNode(name, tableChild.kind(),
                tableChild.location()), tableChild.generated(), tableChild.location(), tableChild.children());
        if (topLevelNode == null) {
            parentTable.addChild(newTableNode);
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

    private TomlTableNode generateTable(Map<String, TopLevelNode> childs, String parentString, boolean isGenerated) {
        TomlKeyNode newTableKey = new TomlKeyNode(parentString, SyntaxKind.STRING_LITERAL, null); //TODO Revisit
        TomlTableNode newTomlTableNode = new TomlTableNode(newTableKey, isGenerated, null);
        childs.put(parentString, newTomlTableNode);
        return newTomlTableNode;
    }

    @Override
    public TomlNode transform(TableNode tableNode) {
        TomlKeyNode tomlKeyNode = transformKey(tableNode.identifier());
        TomlTableNode tomlTableNode = new TomlTableNode(tomlKeyNode, getPosition(tableNode));
        addChildToTable(tableNode, tomlTableNode);
        return tomlTableNode;
    }

    private void addChildToTable(TableNode tableNode, TomlTableNode tomlTableNode) {
        NodeList<Node> childs = tableNode.fields();
        for (Node child : childs) {
            TomlNode transformedChild = child.apply(this); //todo dotted
            if (transformedChild instanceof TopLevelNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
                checkExistingNodes(tomlTableNode, topLevelChild);
                tomlTableNode.addChild(topLevelChild);
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
        TomlKeyNode tomlKeyNode = transformKey(tableArrayNode.identifier());
        TomlTableArrayNode tomlTableArrayNode = new TomlTableArrayNode(tomlKeyNode, getPosition(tableArrayNode));
        TomlTableNode anonTable = addChildsToTableArray(tableArrayNode);
        tomlTableArrayNode.addChild(anonTable);
        return tomlTableArrayNode;
    }

    private TomlTableNode addChildsToTableArray(TableArrayNode tableArrayNode) {
        NodeList<Node> childs = tableArrayNode.fields();
        TomlNodeLocation position = getPosition(tableArrayNode);
        TomlKeyNode anonKey = new TomlKeyNode("Anon", SyntaxKind.UNQUOTED_KEY_TOKEN, position);
        TomlTableNode anonTable = new TomlTableNode(anonKey, position);
        for (Node child : childs) {
            TomlNode transformedChild = child.apply(this); //todo dotted
            if (transformedChild instanceof TopLevelNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
//                checkExistingNodes(tomlTableArray,topLevelChild);
                anonTable.addChild(topLevelChild);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return anonTable;
    }

    @Override
    public TomlNode transform(KeyValue keyValue) {
        TomlKeyNode tomlKeyNode = transformKey(keyValue.identifier());
        ValueNode value = keyValue.value();
        TomlValueNode tomlValue = transformValue(value);

        return new TomlKeyValueNode(tomlKeyNode, tomlValue, getPosition(keyValue));
    }

    private TomlKeyNode transformKey(Token identifierToken) {
        String keyString = identifierToken.text();
        SyntaxKind keyKind = identifierToken.kind();
        TomlNodeLocation keyPosition = getPosition(identifierToken);
        if (keyKind == SyntaxKind.STRING_LITERAL) {
            keyString = keyString.substring(1, keyString.length() - 1);
        }

        TomlKeyNode tomlKeyNode = new TomlKeyNode(keyString, keyKind, keyPosition);
        if (keyString.equals("\"\"")) {
            TomlDiagnostic error = dlog.error(keyPosition, DiagnosticErrorCode.ERROR_EMPTY_QUOTED_STRING);
            tomlKeyNode.addDiagnostic(error);
        }
        return tomlKeyNode;
    }

    private TomlValueNode transformValue(ValueNode valueToken) {
        return (TomlValueNode) valueToken.apply(this);
    }

    @Override
    public TomlNode transform(Array array) {
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
    public TomlNode transform(BasicValueNode basicValueNode) {
        Token valueToken = basicValueNode.value();
        String valueString = valueToken.text();
        SyntaxKind valueKind = valueToken.kind();
        TomlNodeLocation position = getPosition(basicValueNode);

        if (valueKind == SyntaxKind.STRING_LITERAL) {
            return new TomlStringValueNode(valueString.substring(1, valueString.length() - 1),
                    SyntaxKind.STRING_LITERAL, position);
        } else if (valueKind == SyntaxKind.ML_STRING_LITERAL) {
            return new TomlStringValueNode(valueString.substring(3, valueString.length() - 3),
                    SyntaxKind.ML_STRING_LITERAL, position);
        } else if (valueKind == SyntaxKind.DEC_INT) {
            return new TomlLongValueNode(Long.parseLong(valueString), SyntaxKind.DEC_INT, position);
        } else if (valueKind == SyntaxKind.FLOAT) {
            return new TomlDoubleValueNodeNode(Double.parseDouble(valueString), SyntaxKind.FLOAT, position);
        } else if (valueKind == SyntaxKind.BOOLEAN) {
            if (valueString.equals("true")) {
                return new TomlBooleanValueNode(true, SyntaxKind.BOOLEAN, position);
            } else if (valueString.equals("false")) {
                return new TomlBooleanValueNode(false, SyntaxKind.BOOLEAN, position);
            } else {
                throw new UnsupportedOperationException();
            }
        } else if (valueKind == SyntaxKind.BASIC_LITERAL) {
            return new TomlStringValueNode(null, SyntaxKind.BASIC_LITERAL, position);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
