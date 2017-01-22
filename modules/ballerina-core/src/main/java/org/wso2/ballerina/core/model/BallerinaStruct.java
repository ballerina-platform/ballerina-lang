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
 * Ballerina Struct represents a user defined type in ballerina.
 */
public class BallerinaStruct implements Node {

    private SymbolName symbolName;
    private Position structLocation;
    private boolean isPublic;
    private int structMemorySize;
    private VariableDcl[] variableDcls;
    
    /**
     * Create a ballerina struct.
     * 
     * @param name          Name of the struct
     * @param position      Boolean indicating whether the struct is public
     * @param variableDcls  Variables in the struct.
     */
    public BallerinaStruct(SymbolName name,
                             Position position,
                             VariableDcl[] variableDcls) {

        this.symbolName = name;
        this.structLocation = position;
        this.variableDcls = variableDcls;
    }

    /**
     * Get the name of this struct.
     * 
     * @return  Name of this struct
     */
    public String getName() {
        return symbolName.getName();
    }
    
    /**
     * Get the package name of this struct.
     * 
     * @return  Package name of this struct
     */
    public String getPackageName() {
        return symbolName.getPkgName();
    }

    /**
     * Get the function Identifier
     *
     * @return function identifier
     */
    public SymbolName getSymbolName() {
        return symbolName;
    }

    /**
     * Set the symbol name of this struct.
     * 
     * @param symbolName    Symbol name of this struct
     */
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Check whether struct is public, which means function is visible outside the package
     *
     * @return  Flag idicating whether the struct is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Mark function as public
     */
    public void makePublic() {
        isPublic = true;
    }

    public int getStructMemorySize() {
        return structMemorySize;
    }


    public void setStructMemorySize(int structMemorySize) {
        this.structMemorySize = structMemorySize;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    

    /**
     * Get variable attributes in the struct.
     * 
     * @return  Variable attributes in the struct
     */
    public VariableDcl[] getVariableAttributes() {
        return variableDcls;
    }
    
    /**
     * Get the source location of this ballerina Struct.
     * 
     * @return  Source location os this ballerina Struct declaration
     */
    public Position getLocation() {
        return structLocation;
    }

    /**
     * Set the source location of this struct definition.
     * 
     * @param location  Source location of this struct definition.
     */
    public void setLocation(Position location) {
        this.structLocation = location;
    }
    
    /**
     * Builder class to build a Struct.
     */
    public static class StructBuilder {
        private SymbolName structName;
        private VariableDcl[] variableDcls;
        private Position structLocation;

        /**
         * Set the symbol name of this struct.
         * 
         * @param structName    Symbol name of this struct
         */
        public void setStructName(SymbolName structName) {
            this.structName = structName;
        }

        /**
         * Set the attributes of this struct.
         * 
         * @param variableDcls    attributes defined in this struct
         */
        public void setVariableAttributes(VariableDcl[] variableDcls) {
            this.variableDcls = variableDcls;
        }
        
        /**
         * Build the struct.
         * 
         * @return  Struct
         */
        public BallerinaStruct build() {
            return new BallerinaStruct(structName, structLocation, variableDcls);
        }
    }
}
