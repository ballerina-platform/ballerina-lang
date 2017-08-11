/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model;

import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code AnnotationDef} represents a user-defined annotation in Ballerina.
 *
 * @since 0.85
 */
public class AnnotationDef implements CompilationUnit, SymbolScope, BLangSymbol, StructuredUnit {
    private Identifier identifier;
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;
    private AnnotationAttachmentPoint[] attachmentPoints;
    private SymbolName symbolName;
    private AnnotationAttributeDef[] attributes;
    
    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;
    private String pkgName;
    private String pkgPath;
    private AnnotationAttachment[] annotations;

    /**
     * Create an annotation definition.
     * 
     * @param enclosingScope Enclosing scope
     */
    public AnnotationDef(SymbolScope enclosingScope) {
        this.symbolMap = new HashMap<>();
        this.enclosingScope = enclosingScope;
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
     * Set the symbol name of this annotation.
     *
     * @param symbolName Symbol name of this annotation
     */
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }
    
    /**
     * Get the targets to which this annotation can be applied.
     *
     * @return Targets to which this annotation can be applied
     */
    public AnnotationAttachmentPoint[] getAttachmentPoints() {
        return attachmentPoints;
    }

    /**
     * Get attributes defined in this annotation definition.
     * 
     * @return array of attribute definitions in this annotation defintion
     */
    public AnnotationAttributeDef[] getAttributeDefs() {
        return attributes;
    }

    /**
     * Get the package name of the annotation definition.
     * 
     * @return Package name of the annotation definition
     */
    public String getPkgName() {
        return pkgName;
    }

    /**
     * Get the package path of the annotation definition.
     * 
     * @return Package path of the annotation definition
     */
    public String getPkgPath() {
        return pkgPath;
    }
    
    /**
     * Get the annotations attached to this annotation definition.
     * 
     * @return Annotations attached to this annotation definition.
     */
    public AnnotationAttachment[] getAnnotations() {
        return annotations;
    }
    
    @Override
    public BLangSymbol resolveMembers(SymbolName name) {
        return symbolMap.get(name);
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

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
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationDef) {
            AnnotationDef other = (AnnotationDef) obj;
            return this.symbolName.equals(other.symbolName);
        }

        return false;
    }

    // Methods in the BLangSymbol interface
    
    @Override
    public String getName() {
        return identifier.getName();
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public String getPackagePath() {
        return symbolName.getPkgPath();
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isNative() {
        return false;
    }
    
    
    /**
     * Builder class to build an {@code AnnotationDef} node.
     *
     * @since 0.8.0
     */
    public static class AnnotationDefBuilder {
        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;
        private Identifier identifier;
        private String pkgPath;
        private String pkgName;
        private List<AnnotationAttachmentPoint> attachmentPoints = new ArrayList<>();
        private AnnotationDef annotationDef;
        private List<AnnotationAttributeDef> attributes = new ArrayList<>();
        private List<AnnotationAttachment> annotationList = new ArrayList<>();

        /**
         * Create an annotation builder.
         * 
         * @param location Location of the annotation definition in the source.
         * @param enclosingScope Enclosing scope of the annotation
         */
        public AnnotationDefBuilder(NodeLocation location, SymbolScope enclosingScope) {
            annotationDef = new AnnotationDef(enclosingScope);
            this.location = location;
        }

        /**
         * Get current scope.
         * 
         * @return Annotation definition
         */
        public SymbolScope getCurrentScope() {
            return annotationDef;
        }

        public void setIdentifier(Identifier identifier) {
            this.identifier = identifier;
        }

        /**
         * Set the package path of this annotation.
         * 
         * @param pkgPath Package path of this annotation
         */
        public void setPackagePath(String pkgPath) {
            this.pkgPath = pkgPath;
        }
        
        /**
         * Set the package name of this annotation.
         * 
         * @param pkgName Package name of this annotation
         */
        public void setPackageName(String pkgName) {
            this.pkgName = pkgName;
        }
        
        /**
         * Add a field to the annotation.
         *
         * @param attachmentPoint Field in the annotation
         */
        public void addAttachmentPoint(AnnotationAttachmentPoint attachmentPoint) {
            attachmentPoints.add(attachmentPoint);
        }
        
        /**
         * Add an attribute definition to the annotation.
         * 
         * @param attributeDef Attribute definition
         */
        public void addAttributeDef(AnnotationAttributeDef attributeDef) {
            this.attributes.add(attributeDef);
        }

        /**
         * Add an annotation attachment to the annotation.
         * 
         * @param annotation annotation attachment
         */
        public void addAnnotation(AnnotationAttachment annotation) {
            this.annotationList.add(annotation);
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
            return whiteSpaceDescriptor;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        /**
         * Build the annotation definition.
         *
         * @return annotation definition 
         */
        public AnnotationDef build() {
            annotationDef.location = location;
            annotationDef.whiteSpaceDescriptor = whiteSpaceDescriptor;
            annotationDef.identifier = identifier;
            annotationDef.attachmentPoints = attachmentPoints
                    .toArray(new AnnotationAttachmentPoint[attachmentPoints.size()]);
            annotationDef.symbolName = new SymbolName(identifier.getName(), pkgPath);
            annotationDef.pkgName = pkgName;
            annotationDef.pkgPath = pkgPath;
            annotationDef.attributes = attributes.toArray(new AnnotationAttributeDef[attributes.size()]);
            annotationDef.annotations = annotationList.toArray(new AnnotationAttachment[annotationList.size()]);
            return annotationDef;
        }
    }
}
