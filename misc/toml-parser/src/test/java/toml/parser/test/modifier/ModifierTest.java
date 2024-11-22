/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package toml.parser.test.modifier;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlTransformer;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.NodeFactory;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.syntax.tree.ValueNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Contains the test cases for Syntax tree level modifications.
 */
public class ModifierTest {

    private final Path basePath = Path.of("src", "test", "resources", "modifier");

    @Test
    public void testUpdate() throws IOException {
        Path resourceDirectory = basePath.resolve("Dependencies.toml");
        Toml toml = Toml.read(resourceDirectory);

        DocumentNode documentNode = (DocumentNode) toml.rootNode().externalTreeNode();
        NodeList<DocumentMemberDeclarationNode> members = documentNode.members();
        members = members.add(createDependencyEntry());
        documentNode = documentNode.modify().withMembers(members).apply();

        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlTableNode transformedTable = (TomlTableNode) nodeTransformer.transform(documentNode);
        Toml newToml = new Toml(transformedTable);
        Toml newDependency = newToml.getTables("dependency").get(2);
        Assert.assertEquals(((TomlStringValueNode) newDependency.get("org").get()).getValue(), "ballerina");
        Assert.assertEquals(((TomlStringValueNode) newDependency.get("name").get()).getValue(), "haiyo");
        Assert.assertEquals(((TomlStringValueNode) newDependency.get("version").get()).getValue(), "1.0.0");
    }

    private TableArrayNode createDependencyEntry() {

        KeyValueNode org = createKeyValuePairNode("org", "ballerina");
        KeyValueNode name = createKeyValuePairNode("name", "haiyo");
        KeyValueNode version = createKeyValuePairNode("version", "1.0.0");
        return createTomlTableArray("dependency", List.of(org, name, version));
    }

    private TableArrayNode createTomlTableArray(String tableName, List<KeyValueNode> keyValueNodes) {
        Token firstOpenBracketToken = NodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN,
                NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae("\n")),
                NodeFactory.createEmptyMinutiaeList());
        Token openBracketToken = NodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeBracketToken = NodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        SeparatedNodeList<ValueNode> identifier = NodeFactory.createSeparatedNodeList(
                NodeFactory.createIdentifierLiteralNode(NodeFactory.createIdentifierToken(tableName)));

        return NodeFactory.createTableArrayNode(firstOpenBracketToken, openBracketToken,
                NodeFactory.createKeyNode(identifier), closeBracketToken, closeBracketToken,
                NodeFactory.createNodeList(keyValueNodes));
    }

    private KeyValueNode createKeyValuePairNode(String key, String value) {
        SeparatedNodeList<ValueNode> keyNode = NodeFactory.createSeparatedNodeList(
                NodeFactory.createIdentifierLiteralNode(NodeFactory.createIdentifierToken(key,
                        NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae("\n")),
                        NodeFactory.createEmptyMinutiaeList())));

        return NodeFactory
                .createKeyValueNode(NodeFactory.createKeyNode(keyNode), NodeFactory.createToken(SyntaxKind.EQUAL_TOKEN),
                        createStringNode(value));
    }

    private StringLiteralNode createStringNode(String text) {
        Token doubleQuoteToken = NodeFactory.createToken(SyntaxKind.DOUBLE_QUOTE_TOKEN);
        return NodeFactory.createStringLiteralNode(doubleQuoteToken,
                NodeFactory.createIdentifierToken(text), doubleQuoteToken);
    }

}
