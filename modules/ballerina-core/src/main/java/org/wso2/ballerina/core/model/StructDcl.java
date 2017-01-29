/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.model;

/**
 * A {@code Connection} represents the instantiation of a struct with a particular configuration.
 *
 * @since 0.8.0
 */
public class StructDcl implements Node {

    /* Name of the struct to which this struct instance belongs to*/
    SymbolName structName;

    /* Reference to the struct instance which is referred by the declaration */
    StructDef structDef;

    protected NodeLocation location;

    public StructDcl(NodeLocation location, SymbolName structName) {
        this.location = location;
        this.structName = structName;
    }

    /**
     * Get the name of the {@code Connector} which struct is instantiated against
     *
     * @return name of the struct
     */
    public SymbolName getStructName() {
        return structName;
    }

    /**
     * Set the {@code Identifier} of the struct type
     *
     * @param structName Identifier of the struct type
     */
    public void setStructName(SymbolName structName) {
        this.structName = structName;
    }

    /**
     * Set the reference to the struct instance which is referred by the declaration
     *
     * @param structDef reference to the struct instance
     */
    public void setStructDef(StructDef structDef) {
        this.structDef = structDef;
    }

    /**
     * Get the reference to the struct instance which is referred by the declaration
     *
     * @return reference to the struct instance
     */
    public StructDef getStructDef() {
        return structDef;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }
}
