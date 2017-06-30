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

import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.model.types.TypeTags;
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
public class StructDef extends BType implements CompilationUnit, SymbolScope, StructuredUnit {
    private Identifier identifier;
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;
    private AnnotationAttachment[] annotations;
    private VariableDefStmt[] fields;
    private int structMemorySize;
    private SymbolName symbolName;
    private Map<SymbolName, BLangSymbol> symbolMap;
    private BallerinaFunction initFunction;

    public StructDef(SymbolScope enclosingScope) {
        super(null, null, enclosingScope, BStruct.class);
        this.symbolMap = new HashMap<>();
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
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
    public AnnotationAttachment[] getAnnotations() {
        return annotations;
    }

    /**
     * Get variable fields in the struct.
     *
     * @return Variable fields in the struct
     */
    public VariableDefStmt[] getFieldDefStmts() {
        return fields;
    }

    /**
     * Get the size of the allocated memory for the struct.
     *
     * @return Size of the allocated memory
     */
    public int getStructMemorySize() {
        return structMemorySize;
    }

    /**
     * Set the size of memory to allocate for the struct.
     *
     * @param structMemorySize Size of memory to allocate
     */
    public void setStructMemorySize(int structMemorySize) {
        this.structMemorySize = structMemorySize;
    }

    /**
     * Get the struct initializing function.
     *
     * @return Struct initializing function
     */
    public BallerinaFunction getInitFunction() {
        return initFunction;
    }

    /**
     * Set the struct initializing function.
     *
     * @param initFunction Struct initializing function
     */
    public void setInitFunction(BallerinaFunction initFunction) {
        this.initFunction = initFunction;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }


    // Methods in BLangSymbol interface

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return symbolScope;
    }


    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.STRUCT;
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

    @Override
    public BLangSymbol resolveMembers(SymbolName name) {
        return symbolMap.get(name);
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null;
    }

    @Override
    public TypeSignature getSig() {
        String packagePath = (pkgPath == null) ? "." : pkgPath;
        return new TypeSignature(TypeSignature.SIG_STRUCT, packagePath, typeName);
    }

    @Override
    public int getTag() {
        return TypeTags.STRUCT_TAG;
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
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private Identifier identifier;
        private String pkgPath;
        private List<VariableDefStmt> fields = new ArrayList<VariableDefStmt>();
        private List<AnnotationAttachment> annotationList = new ArrayList<>();
        private StructDef structDef;

        public StructBuilder(NodeLocation location, SymbolScope enclosingScope) {
            structDef = new StructDef(enclosingScope);
            this.location = location;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        public SymbolScope getCurrentScope() {
            return structDef;
        }

        public void setIdentifier(Identifier identifier) {
            this.identifier = identifier;
        }

        public void setPackagePath(String pkgPath) {
            this.pkgPath = pkgPath;
        }

        public void addAnnotation(AnnotationAttachment annotation) {
            this.annotationList.add(annotation);
        }

        /**
         * Add a field to the struct.
         *
         * @param field Field in the struct
         */
        public void addField(VariableDefStmt field) {
            fields.add(field);
        }

        /**
         * Build the struct.
         *
         * @return Struct
         */
        public StructDef build() {
            this.structDef.location = location;
            this.structDef.whiteSpaceDescriptor = whiteSpaceDescriptor;
            this.structDef.identifier = identifier;
            this.structDef.typeName = identifier.getName();
            this.structDef.pkgPath = pkgPath;
            this.structDef.annotations = this.annotationList.toArray(
                    new AnnotationAttachment[this.annotationList.size()]);
            this.structDef.fields = fields.toArray(new VariableDefStmt[fields.size()]);
            this.structDef.symbolName = new SymbolName(identifier.getName(), pkgPath);

            return structDef;
        }
    }
}
