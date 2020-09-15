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

package sementic.nodes;

import syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Table Array in TOML AST.
 */
public class TomlTableArray extends TopLevelNode {
    List<TomlTable> childs;
    public TomlTableArray(TomlKey key) {
        super(key);
        this.childs = new ArrayList<>();
    }

    public List<TomlTable> getChilds() {
        return childs;
    }

    public void addChild(TomlTable topLevelNode) {
        childs.add(topLevelNode);
    }

    @Override
    public String toString() {
        return "TomlTableArray{" +
                "identifier=" + key.name +
                '}';
    }

    @Override
    public void accept(TomlNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SyntaxKind getKind() {
        return SyntaxKind.TABLE_ARRAY;
    }
}
