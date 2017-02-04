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

import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BStruct;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerina.core.model.types.TypeConstants.STRUCT_TNAME;

/**
 * {@code Struct} represents a user-defined type in Ballerina.
 *
 * @since 0.8.0
 */
public class StructDef extends BType implements CompilationUnit, SymbolScope {
    private NodeLocation location;
    private String name;
    private String pkgPath;
    private boolean isPublic;
    private int structMemorySize;
    private VariableDef[] fields;

    private SymbolName symbolName;
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    /**
     * Create a ballerina struct.
     *
     * @param name     Name of the struct
     * @param location Boolean indicating whether the struct is public
     * @param fields   Fields in the struct.
     */
    public StructDef(NodeLocation location,
                     String name,
                     String pkgPath,
                     VariableDef[] fields,
                     boolean isPublic,
                     SymbolScope enclosingScope,
                     Map<SymbolName, BLangSymbol> symbolMap) {
        super(STRUCT_TNAME, pkgPath, enclosingScope, BStruct.class);

        this.location = location;
        this.name = name;
        this.pkgPath = pkgPath;
        this.fields = fields;
        this.isPublic = isPublic;

        this.enclosingScope = enclosingScope;
        this.symbolMap = symbolMap;
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    /**
     * Get the name of this struct.
     *
     * @return Name of this struct
     */
    public String getName() {
        return name;
    }

    /**
     * Get the package name of this struct.
     *
     * @return Package name of this struct
     */
    public String getPackagePath() {
        return pkgPath;
    }

    /**
     * Set the symbol name of this struct.
     *
     * @param symbolName Symbol name of this struct
     */
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Check whether Struct is public, which means function is visible outside the package
     *
     * @return Flag indicating whether the struct is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    /**
     * Get variable fields in the struct.
     *
     * @return Variable fields in the struct
     */
    public VariableDef[] getFields() {
        return fields;
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


    // Methods in BLangSymbol interface

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return this;
    }


    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.LOCAL;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return null;
    }

    /**
     * Builder class to build a Struct.
     *
     * @since 0.8.0
     */
    public static class StructBuilder implements SymbolScope {
        private NodeLocation location;
        private String name;
        private String pkgPath;
        private List<VariableDef> fields = new ArrayList<>();
        private boolean isPublic;

        private SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap = new HashMap<>();

        public StructBuilder(SymbolScope enclosingScope) {
            this.enclosingScope = enclosingScope;
        }

        /**
         * Set the symbol name of this struct.
         *
         * @param name Symbol name of this struct
         */
        public void setName(String name) {
            this.name = name;
        }

        public void setPackagePath(String pkgPath) {
            this.pkgPath = pkgPath;
        }

        /**
         * Set the source location of this struct definition.
         *
         * @param location Source location of this struct definition.
         */
        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        /**
         * Set the flag indicating whether this struct is a public one.
         *
         * @param isPublic Flag indicating whether this struct is a public one
         */
        public void setPublic(boolean isPublic) {
            this.isPublic = isPublic;
        }

        /**
         * Add a field to the struct.
         *
         * @param field Field in the struct
         */
        public void addField(VariableDef field) {
            fields.add(field);
        }

        @Override
        public ScopeName getScopeName() {
            return ScopeName.LOCAL;
        }

        @Override
        public SymbolScope getEnclosingScope() {
            return enclosingScope;
        }

        @Override
        public void define(SymbolName name, BLangSymbol symbol) {
            symbolMap.put(name, symbol);
        }

        @Override
        public BLangSymbol resolve(SymbolName name) {
            return resolve(symbolMap, name);
        }

        /**
         * Build the struct.
         *
         * @return Struct
         */
        public StructDef build() {
            return new StructDef(
                    location,
                    name,
                    pkgPath,
                    fields.toArray(new VariableDef[0]),
                    isPublic,
                    enclosingScope,
                    symbolMap);
        }
    }
}
