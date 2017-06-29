/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code ConstDef} represents a Constant in Ballerina.
 *
 * @since 0.8.0
 */
public class ConstDef extends VariableDef implements CompilationUnit {
    private Expression rhsExpr;
    private BValue value;
    private List<AnnotationAttachment> annotations;

    public ConstDef(NodeLocation location,
                    WhiteSpaceDescriptor whiteSpaceDescriptor,
                    Identifier identifier,
                    SimpleTypeName typeName,
                    String pkgPath,
                    SymbolName symbolName,
                    SymbolScope symbolScope,
                    Expression rhsExpr) {

        super(location, whiteSpaceDescriptor, identifier, typeName, symbolName, symbolScope);
        this.pkgPath = pkgPath;
        this.rhsExpr = rhsExpr;
        this.annotations = new ArrayList<>();
    }

    public Expression getRhsExpr() {
        return rhsExpr;
    }

    public void setRhsExpr(Expression rhsExpr) {
        this.rhsExpr = rhsExpr;
    }

    public BValue getValue() {
        return value;
    }

    public void setValue(BValue value) {
        this.value = value;
    }

    /**
     * Add an annotation to the constant.
     * 
     * @param annotation Annotation attachment
     */
    public void addAnnotation(AnnotationAttachment annotation) {
        this.annotations.add(annotation);
    }
    
    /**
     * Get all the Annotations associated with this constant.
     *
     * @return List of annotation attachments
     */
    public AnnotationAttachment[] getAnnotations() {
        return this.annotations.toArray(new AnnotationAttachment[annotations.size()]);
    }
    
    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
