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

package sementic;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import sementic.nodes.DiagnosticLog;
import sementic.nodes.DiagnosticPos;
import sementic.nodes.DiagnosticSource;
import sementic.nodes.TomlArray;
import sementic.nodes.TomlBasicValue;
import sementic.nodes.TomlKey;
import sementic.nodes.TomlKeyValue;
import sementic.nodes.TomlNode;
import sementic.nodes.TomlTable;
import sementic.nodes.TomlTableArray;
import sementic.nodes.TomlValue;
import sementic.nodes.TopLevelNode;
import sementic.tools.DiagnosticCode;
import syntax.tree.Array;
import syntax.tree.BasicValueNode;
import syntax.tree.KeyValue;
import syntax.tree.ModuleMemberDeclarationNode;
import syntax.tree.ModulePartNode;
import syntax.tree.Node;
import syntax.tree.NodeList;
import syntax.tree.NodeTransformer;
import syntax.tree.SeparatedNodeList;
import syntax.tree.SyntaxKind;
import syntax.tree.TableArrayNode;
import syntax.tree.TableNode;
import syntax.tree.Token;
import syntax.tree.ValueNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Transformer to transform Syntax tree into Abstract Syntax Tree.
 */
public class TomlTransformer extends NodeTransformer<TomlNode> {

    private DiagnosticSource diagnosticSource;
    private DiagnosticLog dlog;

    public TomlTransformer(DiagnosticSource diagnosticSource) {

        this.diagnosticSource = diagnosticSource;
        this.dlog = DiagnosticLog.getInstance();
    }

    @Override
    public TomlNode transform(ModulePartNode modulePartNode) {
        TomlKey tomlKey = new TomlKey("RootNode", SyntaxKind.MODULE_NAME);
        TomlTable rootTable = new TomlTable(tomlKey);

        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (ModuleMemberDeclarationNode rootNode : members) {
            TomlNode transformedChild = rootNode.apply(this); //todo dotted
            addChildNodeToParent(rootTable, transformedChild);
        }
        return rootTable;
    }

    private void addChildNodeToParent(TomlTable rootTable, TomlNode transformedChild) {
        if (transformedChild instanceof TopLevelNode) {
            if (transformedChild instanceof TomlTable) {
                TomlTable tableChild = (TomlTable) transformedChild;
                addChildTableToParent(rootTable, tableChild);
            } else if (transformedChild instanceof TomlTableArray) {
                TomlTableArray transformedArray = (TomlTableArray) transformedChild;
                addChildParentArrayToParent(rootTable, transformedArray);
            } else if (transformedChild instanceof TomlKeyValue) {
                TomlKeyValue transformedKeyValuePair = (TomlKeyValue) transformedChild;
                addChildKeyValueToParent(rootTable, transformedKeyValuePair);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void addChildKeyValueToParent(TomlTable rootTable, TomlKeyValue transformedKeyValuePair) {
        String key = transformedKeyValuePair.getKey().name;
        String[] split = splitDottedKey(key);
        String[] parentTables = getParentTables(split);
        TomlTable parentTable = rootTable;
        ArrayList<String> parentKeys = new ArrayList<>();
        for (String newTable : parentTables) {
            parentKeys.add(newTable);
            String dottedKey = createDottedKeyFromList(parentKeys);
            TopLevelNode dottedParentNode = parentTable.getChilds().get(dottedKey);
            if (dottedParentNode != null) {
                //TOOD fix
            } else {
                //create the table
                parentTable = createDottedKeyParentTable(parentTable, dottedKey);

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
        parentTable.getChilds().put(dottedKey, newTomlTable);
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
        TopLevelNode topLevelNode = parentTable.getChilds().get(tableArrayChild.key.name);
        if (topLevelNode == null) {
            parentTable.addChild(tableArrayChild);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTableArray) {
                ((TomlTableArray) topLevelNode).addChild(tableArrayChild.getChilds().get(0));
            } else if (topLevelNode instanceof TomlKeyValue) {
                dlog.error(tableArrayChild.pos, "Node Exists", DiagnosticCode.EXISTING_NODE);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private TomlTable getParentTable(TomlTable rootTable, TopLevelNode tableArrayChild) {
        String identifierName = tableArrayChild.key.name;
        String[] split = splitDottedKey(identifierName);
        String tableLeadName = getLastKeyOfDottedKey(split);
        String[] parentTables = getParentTables(split);

        TomlTable parentTable = rootTable;
        Map<String, TopLevelNode> rootTableNodes = rootTable.getChilds();
        List<String> parentTablesString = new ArrayList<>();
        for (String parentString : parentTables) {
            parentTablesString.add(parentString);
            String dottedParentString = createDottedKeyFromList(parentTablesString);
            TopLevelNode rootTableNode = parentTable.getChilds().get(dottedParentString);
            if (rootTableNode != null) {
                parentTable = (TomlTable) rootTableNode;
            } else {
                if (tableArrayChild instanceof TomlTableArray) {
                    parentTable = generateTable(parentTable.getChilds(), dottedParentString, false);
                } else {
                    parentTable = generateTable(parentTable.getChilds(), dottedParentString, true);
                }
            }
        }

        TopLevelNode lastNode = parentTable.getChilds().get(tableLeadName);
        if (lastNode instanceof TomlKeyValue) {
            dlog.error(tableArrayChild.pos, "Node Exists", DiagnosticCode.EXISTING_NODE); //TODO revisit dotted.
        }
        return parentTable;
    }

    private String getLastKeyOfDottedKey(String[] split) {
        return split[split.length - 1];
    }

    private void addChildTableToParent(TomlTable rootTable, TomlTable tableChild) {
        TomlTable parentTable = getParentTable(rootTable, tableChild);
        TopLevelNode topLevelNode = parentTable.getChilds().get(tableChild.key.name);
        if (topLevelNode == null) {
            parentTable.addChild(tableChild);
        } else {
            //generated false?
            if (topLevelNode instanceof TomlTable) {
                if (((TomlTable) topLevelNode).generated) {
                    parentTable.replaceGeneratedTable(tableChild);
                } else {
                    dlog.error(tableChild.pos, "Node Exists", DiagnosticCode.EXISTING_NODE);
                }
            } else if (topLevelNode instanceof TomlKeyValue) {
                dlog.error(tableChild.pos, "Node Exists", DiagnosticCode.EXISTING_NODE);
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
        tomlTable.pos = getPosition(tableNode);
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
        Map<String, TopLevelNode> childs = tomlTable.getChilds();
        String childName = topLevelChild.getKey().name;
        if (childs.get(childName) != null) {
            dlog.error(topLevelChild.pos, "Node Exists", DiagnosticCode.EXISTING_NODE);
        }
    }

    @Override
    public TomlNode transform(TableArrayNode tableArrayNode) {
        TomlKey tomlKey = transformKey(tableArrayNode.identifier());
        TomlTableArray tomlTableArray = new TomlTableArray(tomlKey);
        tomlTableArray.pos = getPosition(tableArrayNode);
        TomlTable anonTable = addChildsToTableArray(tableArrayNode);
        tomlTableArray.addChild(anonTable);
        return tomlTableArray;
    }

    private TomlTable addChildsToTableArray(TableArrayNode tableArrayNode) {
        NodeList<Node> childs = tableArrayNode.fields();
        TomlKey anonKey = new TomlKey("Anon", SyntaxKind.UNQUOTED_KEY_TOKEN);
        TomlTable anonTable = new TomlTable(anonKey);
        anonTable.pos = getPosition(tableArrayNode);
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
        keyValuePair.pos = getPosition(keyValue);

        return keyValuePair;
    }

    private TomlKey transformKey(Token identifierToken) {
        String keyString = identifierToken.text();
        SyntaxKind keyKind = identifierToken.kind();
        DiagnosticPos keyPosition = getPosition(identifierToken);
        if (keyKind == SyntaxKind.STRING_LITERAL) {
            keyString = keyString.substring(1, keyString.length() - 1);
            if (keyString.equals("\"\"")) {
                dlog.error(keyPosition, "Quoted String Key can't be empty", DiagnosticCode.EMPTY_KEY);
            }
        }

        TomlKey tomlKey = new TomlKey(keyString, keyKind);
        tomlKey.pos = keyPosition;
        return tomlKey;
    }

    private TomlValue transformValue(ValueNode valueToken) {
        SyntaxKind valueKind = valueToken.kind();
        TomlValue transformedValue = (TomlValue) valueToken.apply(this);
//        TomlValue tomlKey = new TomlValue(value, valueKind);
        transformedValue.pos = getPosition(valueToken);
        return transformedValue;
    }

    @Override
    public TomlNode transform(Array array) {
        SeparatedNodeList<ValueNode> values = array.values();
        List<TomlValue> elements = new ArrayList<>();
        for (ValueNode value : values) {
            TomlValue transformedValue = (TomlValue) value.apply(this);
            //Object converedValue = convertBasicValue(value);
            elements.add(transformedValue);
        }
        return new TomlArray(elements);
    }

    @Override
    protected TomlNode transformSyntaxNode(Node node) {
        return null;
    }

    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange lineRange = node.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line() + 1, endPos.line() + 1, startPos.offset() + 1,
                endPos.offset() + 1);
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
        } else {
            throw new UnsupportedOperationException();
        }
        return output;
    }
}
