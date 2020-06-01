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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a syntax node element in the syntax tree descriptor.
 *
 * @since 1.3.0
 */
public class SyntaxNode {
    private String name;
    private List<SyntaxNodeAttribute> attributes;
    private String kind;
    private String base;
    private String type;
    private boolean isAbstract;

    public SyntaxNode(String name,
                      List<SyntaxNodeAttribute> attributes,
                      String kind,
                      String base,
                      String type,
                      boolean isAbstract) {
        this.name = name;
        this.attributes = attributes;
        this.kind = kind;
        this.base = base;
        this.type = type;
        this.isAbstract = isAbstract;
    }

    public SyntaxNode() {
        this.attributes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<SyntaxNodeAttribute> getAttributes() {
        return attributes;
    }

    public String getBase() {
        return base;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public boolean isAbstract() {
        return isAbstract;
    }
}
