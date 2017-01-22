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

import org.wso2.ballerina.core.model.expressions.Expression;

import java.util.List;

/**
 * A {@code Connection} represents the instantiation of a struct with a particular configuration.
 *
 * @since 1.0.0
 */
public class StructDcl implements Node {

    /* Name of the struct which Connection is instantiated against */
    SymbolName structName;

    /* Name of the struct instance */
    // TODO: remove if unused
    SymbolName varName;

    /* Reference to the struct instance which is referred by the declaration */
    BallerinaStruct struct;
    
    protected Position sourceLocation;

    public StructDcl(SymbolName structName, SymbolName varName) {
        this.structName = structName;
        this.varName = varName;
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
     * Get the {@code Identifier} of the struct instance
     *
     * @return identifier of the struct instance
     */
    public SymbolName getVarName() {
        return varName;
    }
    
    /**
     * Set the {@code Identifier} of the struct instance
     *
     * @param varName   Identifier of the struct instance
     */
    public void setVarName(SymbolName varName) {
        this.varName = varName;
    }

    /**
     * Set the {@code Identifier} of the struct type
     *
     * @param structName   Identifier of the struct type
     */
    public void setStructName(SymbolName structName) {
        this.structName = structName;
    }

    /**
     * Set the reference to the struct instance which is referred by the declaration
     *
     * @param struct reference to the struct instance
     */
    public void setStruct(BallerinaStruct struct) {
        this.struct = struct;
    }

    /**
     * Get the reference to the struct instance which is referred by the declaration
     *
     * @return reference to the struct instance
     */
    public BallerinaStruct getStruct() {
        return struct;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     *
     */
    public static class StructDclBuilder {
        private SymbolName structName;
        private SymbolName varName;
        private List<Expression> exprList;

        public void setStructName(SymbolName structName) {
            this.structName = structName;
        }

        public void setVarName(SymbolName varName) {
            this.varName = varName;
        }

        public StructDcl build() {
            return new StructDcl(
                    structName,
                    varName);
        }
    }
    
    /**
     * Get the source location of this struct declaration.
     * Return the source file and the line number of this struct declaration.
     * 
     * @return  Source location of this struct declaration
     */
    public Position getLocation() {
        return sourceLocation;
    }

    /**
     * Set the source location of this struct declaration.
     * 
     * @param location  Source location of this struct declaration.
     */
    public void setLocation(Position location) {
        this.sourceLocation = location;
    }
}
