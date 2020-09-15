/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Set;

/**
 * Represents a TOML Node AST.
 */
public abstract class TomlNode implements Node {

    /**
     * The type of this node.
     */
    //public BType type;
    public TomlNode parent = null;

    /**
     * Indicates whether the node has already been desugared.
     */
    public boolean desugared;

    /**
     * The position of this node in the source file.
     */
    public DiagnosticPos pos;
    private Set<Whitespace> ws;

    public DiagnosticPos getPosition() {
        return pos;
    }

    public abstract void accept(TomlNodeVisitor visitor);

    @Override
    public Set<Whitespace> getWS() {
        return ws;
    }

    @Override
    public void addWS(Set<Whitespace> whitespaces) {
        if (this.ws == null) {
            this.ws = whitespaces;
        } else if (whitespaces != null) {
            this.ws.addAll(whitespaces);
        }
    }
}
