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
package io.ballerinalang.compiler.internal.treegen.model.json;

import java.util.List;

/**
 * Represents the syntax tree in the syntax tree descriptor.
 *
 * @since 1.3.0
 */
public class SyntaxTree {
    private final String root;
    private final List<SyntaxNode> nodes;
    private final String type;
    private final List<SyntaxNodeAttribute> fields;

    public SyntaxTree(String root,
                      List<SyntaxNode> nodes,
                      String type,
                      List<SyntaxNodeAttribute> fields) {
        this.root = root;
        this.nodes = nodes;
        this.type = type;
        this.fields = fields;
    }

    public String getRoot() {
        return root;
    }

    public List<SyntaxNode> nodes() {
        return nodes;
    }

    public String getType() {
        return type;
    }

    public List<SyntaxNodeAttribute> getFields() {
        return fields;
    }
}
