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

package org.ballerinalang.model;

import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code Struct} represents a user-defined type in Ballerina.
 *
 * @since 0.8.0
 */
public class StructDef extends BType implements CompilationUnit, SymbolScope {
    private NodeLocation location;
    private Annotation[] annotations;
    private VariableDef[] fields;
    private int structMemorySize;

    private SymbolName symbolName;
    private Map<SymbolName, BLangSymbol> symbolMap;

    public StructDef(SymbolScope enclosingScope) {
        super(null, null, enclosingScope, BStruct.class);
        this.symbolMap = new HashMap<>();
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
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
     * Get all the Annotations associated with this struct.
     *
     * @return list of Annotations
     */
    public Annotation[] getAnnotations() {
        return annotations;
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
        return symbolScope;
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
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    public BLangSymbol resolveMembers(SymbolName name) {
        return symbolMap.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof StructDef) {
            StructDef other = (StructDef) obj;
            return this.typeName.equals(other.typeName);
        }

        return false;
    }

    /**
     * Builder class to build a Struct.
     *
     * @since 0.8.0
     */
    public static class StructBuilder {
        private NodeLocation location;
        private String name;
        private String pkgPath;
        private List<VariableDef> fields = new ArrayList<>();
        private List<Annotation> annotationList = new ArrayList<>();
        private StructDef structDef;

        public StructBuilder(NodeLocation location, SymbolScope enclosingScope) {
            structDef = new StructDef(enclosingScope);
            this.location = location;
        }

        public SymbolScope getCurrentScope() {
            return structDef;
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
        
        public void addAnnotation(Annotation annotation) {
            this.annotationList.add(annotation);
        }

        /**
         * Add a field to the struct.
         *
         * @param field Field in the struct
         */
        public void addField(VariableDef field) {
            fields.add(field);
        }

        /**
         * Build the struct.
         *
         * @return Struct
         */
        public StructDef build() {
            this.structDef.location = location;
            this.structDef.typeName = name;
            this.structDef.pkgPath = pkgPath;
            this.structDef.annotations = this.annotationList.toArray(new Annotation[this.annotationList.size()]);
            this.structDef.fields = fields.toArray(new VariableDef[fields.size()]);
            this.structDef.symbolName = new SymbolName(name, pkgPath);

            return structDef;
        }
    }
}
