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

import java.util.HashMap;
import java.util.Map;

/**
 * {@code AnnotationDef} represents a user-defined annotation in Ballerina.
 *
 * @since 0.85
 */
public class AnnotationDef implements CompilationUnit, BLangSymbol {
    private Identifier identifier;
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;
    private AnnotationAttachmentPoint[] attachmentPoints;
    private SymbolName symbolName;
    private AnnotationAttributeDef[] attributes;
    
    // Scope related variables
    private Map<SymbolName, BLangSymbol> symbolMap;
    private String pkgName;
    private String pkgPath;
    private AnnotationAttachment[] annotations;

    /**
     * Create an annotation definition.
     */
    public AnnotationDef() {
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
    public SymbolName getSymbolName() {
        return symbolName;
    }

    // Methods in the SymbolScope interface

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
}
