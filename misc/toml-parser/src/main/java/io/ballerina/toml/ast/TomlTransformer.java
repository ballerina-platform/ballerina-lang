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
        TomlKey tomlKey = new TomlKey("RootNode", SyntaxKind.MODULE_NAME);
        TomlTable rootTable = new TomlTable(tomlKey);

        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (ModuleMemberDeclarationNode rootNode : members) {
            TomlNode transformedChild = rootNode.apply(this);
            addChildNodeToParent(rootTable, transformedChild);
        }
        return rootTable;
    }

    private void addChildNodeToParent(TomlTable rootTable, TomlNode transformedChild) {
        if (transformedChild.getKind() == SyntaxKind.TABLE) {
            TomlTable tableChild = (TomlTable) transformedChild;
            addChildTableToParent(rootTable, tableChild);
        } else if (transformedChild.getKind() == SyntaxKind.TABLE_ARRAY) {
            TomlTableArray transformedArray = (TomlTableArray) transformedChild;
            addChildParentArrayToParent(rootTable, transformedArray);
        } else if (transformedChild.getKind() == SyntaxKind.KEY_VALUE) {
            TomlKeyValue transformedKeyValuePair = (TomlKeyValue) transformedChild;
            addChildKeyValueToParent(rootTable, transformedKeyValuePair);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void addChildKeyValueToParent(TomlTable rootTable, TomlKeyValue transformedKeyValuePair) {
        String key = transformedKeyValuePair.getKey().name;
        String[] split = splitDottedKey(key);
        String[] parentTables = getParentTables(split);
        TomlTable parentTable = rootTable;
        for (String newTable : parentTables) {
            TopLevelNode dottedParentNode = parentTable.getChildren().get(newTable);
            if (dottedParentNode != null) {
                //TOOD fix
            } else {
                //create the table
                parentTable = createDottedKeyParentTable(parentTable, newTable);
            }
        }
        if (split.length > 1) {
            TomlKey newKey = new TomlKey(split[split.length - 1], SyntaxKind.STRING_LITERAL);
            transformedKeyValuePair.setKey(newKey);
        }
        parentTable.addChild(transformedKeyValuePair);
    }

    private String createDottedKeyFromList(List<String> parentKeys) {
        return String.join(".", parentKeys);
    }

    private TomlTable createDottedKeyParentTable(TomlTable parentTable, String dottedKey) {
        TomlKey newTableKey = new TomlKey(dottedKey, SyntaxKind.STRING_LITERAL);
        TomlTable newTomlTable = new TomlTable(newTableKey);
        newTomlTable.setGenerated(false);
        parentTable.getChildren().put(dottedKey, newTomlTable);
        return newTomlTable;
    }

    private String[] getParentTables(String[] split) {
        return Arrays.copyOf(split, split.length - 1);
    }

    private String[] splitDottedKey(String key) {
        return key.split("\\.");
    }

    private void addChildParentArrayToParent(TomlTable rootTable, TomlTableArray tableArrayChild) {
        TomlTable parentTable = getParentTable(rootTable, tableArrayChild);
        //
        String[] split = tableArrayChild.getKey().name.split("\\.");
        String name = split[split.length - 1];
        tableArrayChild.setKey(new TomlKey(name, tableArrayChild.kind));
        TopLevelNode topLevelNode = parentTable.getChildren().get(tableArrayChild.key.name);
        if (topLevelNode == null) {
            parentTable.addChild(tableArrayChild);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTableArray) {
                ((TomlTableArray) topLevelNode).addChild(tableArrayChild.getChilds().get(0));
            } else if (topLevelNode instanceof TomlKeyValue) {
                TomlDiagnostic nodeExists =
                        dlog.error(tableArrayChild.location, DiagnosticErrorCode.ERROR_EXISTING_NODE);
                parentTable.addDiagnostic(nodeExists);

            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private TomlTable getParentTable(TomlTable rootTable, TopLevelNode childNode) {
        String identifierName = childNode.key.name;
        String[] split = splitDottedKey(identifierName);
        String tableLeadName = getLastKeyOfDottedKey(split);
        String[] parentTables = getParentTables(split);

        TomlTable parentTable = rootTable;
        List<String> parentTablesString = new ArrayList<>();
        for (String parentString : parentTables) {
            parentTablesString.add(parentString);
            String dottedParentString = createDottedKeyFromList(parentTablesString);
            TopLevelNode rootTableNode = parentTable.getChildren().get(dottedParentString);
            if (rootTableNode != null) {
                parentTable = (TomlTable) rootTableNode;
            } else {
                if (childNode instanceof TomlTableArray) {
                    parentTable = generateTable(parentTable.getChildren(), dottedParentString, false);
                } else {
                    parentTable = generateTable(parentTable.getChildren(), dottedParentString, true);
                }
            }
        }

        TopLevelNode lastNode = parentTable.getChildren().get(tableLeadName);
        if (lastNode instanceof TomlKeyValue) {
            TomlDiagnostic nodeExists =
                    dlog.error(childNode.location, DiagnosticErrorCode.ERROR_EXISTING_NODE);
            //TODO revisit dotted.
            parentTable.addDiagnostic(nodeExists);
        }
        return parentTable;
    }

    private String getLastKeyOfDottedKey(String[] split) {
        return split[split.length - 1];
    }

    private void addChildTableToParent(TomlTable rootTable, TomlTable tableChild) {
        TomlTable parentTable = getParentTable(rootTable, tableChild);
        TopLevelNode topLevelNode = parentTable.getChildren().get(tableChild.key.name);
        String[] split = tableChild.getKey().name.split("\\.");
        String name = split[split.length - 1];
        tableChild.setKey(new TomlKey(name, tableChild.kind));
        if (topLevelNode == null) {
            parentTable.addChild(tableChild);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTable) {
                if (((TomlTable) topLevelNode).generated) {
                    parentTable.replaceGeneratedTable(tableChild);
                } else {
                    TomlDiagnostic nodeExist = dlog.error(tableChild.location, DiagnosticErrorCode.ERROR_EXISTING_NODE);
                    parentTable.addDiagnostic(nodeExist);
                }
            } else if (topLevelNode instanceof TomlKeyValue) {
                TomlDiagnostic nodeExists = dlog.error(tableChild.location, DiagnosticErrorCode.ERROR_EXISTING_NODE);
                parentTable.addDiagnostic(nodeExists);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private TomlTable generateTable(Map<String, TopLevelNode> childs, String parentString, boolean isGenerated) {
        TomlKey newTableKey = new TomlKey(parentString, SyntaxKind.STRING_LITERAL);
        TomlTable newTomlTable = new TomlTable(newTableKey);
        newTomlTable.setGenerated(isGenerated);
        childs.put(parentString, newTomlTable);
        return newTomlTable;
    }

    @Override
    public TomlNode transform(TableNode tableNode) {
        TomlKey tomlKey = transformKey(tableNode.identifier());
        TomlTable tomlTable = new TomlTable(tomlKey);
        tomlTable.location = getPosition(tableNode);
        addChildToTable(tableNode, tomlTable);
        return tomlTable;
    }

    private void addChildToTable(TableNode tableNode, TomlTable tomlTable) {
        NodeList<Node> childs = tableNode.fields();
        for (Node child : childs) {
            TomlNode transformedChild = child.apply(this); //todo dotted
            if (transformedChild instanceof TopLevelNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
                checkExistingNodes(tomlTable, topLevelChild);
                tomlTable.addChild(topLevelChild);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void checkExistingNodes(TomlTable tomlTable, TopLevelNode topLevelChild) {
        Map<String, TopLevelNode> childs = tomlTable.getChildren();
        String childName = topLevelChild.getKey().name;
        if (childs.get(childName) != null) {
            TomlDiagnostic nodeExists = dlog.error(topLevelChild.location, DiagnosticErrorCode.ERROR_EXISTING_NODE);
            tomlTable.addDiagnostic(nodeExists);
        }
    }

    @Override
    public TomlNode transform(TableArrayNode tableArrayNode) {
        TomlKey tomlKey = transformKey(tableArrayNode.identifier());
        TomlTableArray tomlTableArray = new TomlTableArray(tomlKey);
        tomlTableArray.location = getPosition(tableArrayNode);
        TomlTable anonTable = addChildsToTableArray(tableArrayNode);
        tomlTableArray.addChild(anonTable);
        return tomlTableArray;
    }

    private TomlTable addChildsToTableArray(TableArrayNode tableArrayNode) {
        NodeList<Node> childs = tableArrayNode.fields();
        TomlKey anonKey = new TomlKey("Anon", SyntaxKind.UNQUOTED_KEY_TOKEN);
        TomlTable anonTable = new TomlTable(anonKey);
        anonTable.location = getPosition(tableArrayNode);
        for (Node child : childs) {
            TomlNode transformedChild = child.apply(this); //todo dotted
            if (transformedChild instanceof TopLevelNode) {
                TopLevelNode topLevelChild = (TopLevelNode) transformedChild;
                //checkExistingNodes(tomlTableArray,topLevelChild);
                anonTable.addChild(topLevelChild);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return anonTable;
    }

    @Override
    public TomlNode transform(KeyValue keyValue) {
        TomlKey tomlKey = transformKey(keyValue.identifier());
        ValueNode value = keyValue.value();
        TomlValue tomlValue = transformValue(value);

        TomlKeyValue keyValuePair = new TomlKeyValue(tomlKey, tomlValue);
        keyValuePair.location = getPosition(keyValue);

        return keyValuePair;
    }

    private TomlKey transformKey(Token identifierToken) {
        String keyString = identifierToken.text();
        SyntaxKind keyKind = identifierToken.kind();
        TomlNodeLocation keyPosition = getPosition(identifierToken);
        if (keyKind == SyntaxKind.STRING_LITERAL) {
            keyString = keyString.substring(1, keyString.length() - 1);
        }

        TomlKey tomlKey = new TomlKey(keyString, keyKind);
        if (keyString.equals("\"\"")) {
            TomlDiagnostic error = dlog.error(keyPosition, DiagnosticErrorCode.ERROR_EMPTY_QUOTED_STRING);
            tomlKey.addDiagnostic(error);
        }
        tomlKey.location = keyPosition;
        return tomlKey;
    }

    private TomlValue transformValue(ValueNode valueToken) {
        TomlValue transformedValue = (TomlValue) valueToken.apply(this);
        transformedValue.location = getPosition(valueToken);
        return transformedValue;
    }

    @Override
    public TomlNode transform(Array array) {
        SeparatedNodeList<ValueNode> values = array.values();
        List<TomlValue> elements = new ArrayList<>();
        for (ValueNode value : values) {
            TomlValue transformedValue = (TomlValue) value.apply(this);
            elements.add(transformedValue);
        }
        return new TomlArray(elements);
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
        Object value = convertBasicValue(basicValueNode.value());
        return new TomlBasicValue(value, basicValueNode.kind());
    }

    private Object convertBasicValue(Token valueToken) {
        String valueString = valueToken.text();
        SyntaxKind valueKind = valueToken.kind();

        Object output;
        if (valueKind == SyntaxKind.STRING_LITERAL) {
            output = valueString.substring(1, valueString.length() - 1);
        } else if (valueKind == SyntaxKind.ML_STRING_LITERAL) {
            output = valueString.substring(3, valueString.length() - 3);
        } else if (valueKind == SyntaxKind.DEC_INT) {
            output = Long.parseLong(valueString);
        } else if (valueKind == SyntaxKind.FLOAT) {
            output = Double.parseDouble(valueString);
        } else if (valueKind == SyntaxKind.BOOLEAN) {
            if (valueString.equals("true")) {
                output = true;
            } else if (valueString.equals("false")) {
                output = false;
            } else {
                throw new UnsupportedOperationException();
            }
        } else if (valueKind == SyntaxKind.BASIC_LITERAL) {
            output = null;
        } else {
            throw new UnsupportedOperationException();
        }
        return output;
    }
}
