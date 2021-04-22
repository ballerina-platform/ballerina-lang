/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes the syntax tree format that is expected.
 * This inputs directly from api_gen_syntax_tree_descriptor file.
 *
 * @since 2.0.0
 */
public class SyntaxTreeDescriptor {
    private List<Node> nodes;

    /**
     * Collects and returns child names from the descriptor.
     * This will return a map of format NodeTypeName -> ChildrenParamsNames
     */
    public Map<String, List<String>> getChildNames() {
        Map<String, List<String>> childNames = new HashMap<>();
        for (Node node : nodes) {
            List<String> nameList = new ArrayList<>();
            childNames.put(node.name, nameList);
            for (Attribute attribute : node.attributes) {
                nameList.add(attribute.name);
            }
        }
        return childNames;
    }

    private static class Attribute {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class Node {
        private String name;
        private List<Attribute> attributes;

        private Node() {
            this.attributes = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }
    }
}
