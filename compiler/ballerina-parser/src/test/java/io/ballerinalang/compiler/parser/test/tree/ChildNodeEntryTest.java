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
package io.ballerinalang.compiler.parser.test.tree;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains cases to test {@code ChildNodeEntry} functionality.
 *
 * @since 1.3.0
 */
public class ChildNodeEntryTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testChildNodeEntry() {
        Map<String, Object> expectedChildEntryMap = loadChildEntryMap("child_node_entry_test_01.json");
        SyntaxTree syntaxTree = parseFile("child_node_entry_test_01.bal");
        MapGen mapGen = new MapGen();
        Map<String, Object> actualChildEntryMap = mapGen.transformSyntaxNode(syntaxTree.rootNode());
        Assert.assertEqualsDeep(actualChildEntryMap, expectedChildEntryMap);
    }

    private Map<String, Object> loadChildEntryMap(String filePath) {
        String sourceText = getFileContentAsString(filePath);
        Gson gson = new Gson();
        return (Map<String, Object>) gson.fromJson(sourceText, Map.class);
    }

    /**
     * An implementation of the {@code NodeTransformer} that transforms the syntax tree into a {@code Map}.
     *
     * @since 1.3.0
     */
    public static class MapGen extends NodeTransformer<Map<String, Object>> {
        @Override
        protected Map<String, Object> transformSyntaxNode(Node node) {
            Map<String, Object> childEntryMap = new LinkedTreeMap<>();
            NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
            for (ChildNodeEntry childNodeEntry : nonTerminalNode.childEntries()) {
                if (childNodeEntry.isList()) {
                    List<Object> childList = new ArrayList<>();
                    for (Node listChildNode : childNodeEntry.nodeList()) {
                        childList.add(apply(listChildNode));
                    }
                    childEntryMap.put(childNodeEntry.name(), childList);
                } else if (childNodeEntry.node().isPresent()) {
                    childEntryMap.put(childNodeEntry.name(), apply(childNodeEntry.node().get()));
                }
            }
            return childEntryMap;
        }

        private Map<String, Object> apply(Node node) {
            Map<String, Object> nodeInfo = new LinkedHashMap<>();
            nodeInfo.put("kind", node.kind().toString());
            if (node instanceof Token) {
                nodeInfo.put("value", ((Token) node).text());
            } else {
                nodeInfo.put("value", node.apply(this));
            }
            return nodeInfo;
        }
    }
}
