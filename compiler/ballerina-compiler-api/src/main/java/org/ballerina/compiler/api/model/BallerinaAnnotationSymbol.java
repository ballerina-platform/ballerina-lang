/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.api.model;

import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent Function Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaAnnotationSymbol extends BallerinaTypeDefinition {
    
    private List<AnnotationAttachPoint> attachPoints;
    
    private BallerinaAnnotationSymbol(String name,
                                      PackageID moduleID,
                                      List<AccessModifier> accessModifiers,
                                      TypeDescriptor typeDescriptor,
                                      List<AnnotationAttachPoint> attachPoints,
                                      BSymbol bSymbol) {
        super(name, moduleID, accessModifiers, BallerinaSymbolKind.ANNOTATION, typeDescriptor, bSymbol);
        this.attachPoints = attachPoints;
    }

    /**
     * Get the attached points.
     * 
     * @return {@link List} of attached points 
     */
    public List<AnnotationAttachPoint> getAttachPoints() {
        return attachPoints;
    }

    /**
     * Represents Ballerina Annotation Symbol Builder.
     * 
     * @since 1.3.0
     */
    public static class AnnotationSymbolBuilder extends TypeDefSymbolBuilder {
        
        private List<AnnotationAttachPoint> attachPoints = new ArrayList<>();
        
        public AnnotationSymbolBuilder(String name, PackageID moduleID, BAnnotationSymbol annotationSymbol) {
            super(name, moduleID, annotationSymbol);
            withAnnotationSymbol(annotationSymbol);
        }

        public BallerinaAnnotationSymbol build() {
            return new BallerinaAnnotationSymbol(this.name,
                    this.moduleID,
                    this.accessModifiers,
                    this.typeDescriptor,
                    this.attachPoints,
                    this.bSymbol);
        }

        /**
         * Set the attach points from the annotation symbol.
         * 
         * @param annotationSymbol annotation symbol to evaluate
         */
        private void withAnnotationSymbol(BAnnotationSymbol annotationSymbol) {
            this.attachPoints.addAll(AnnotationAttachPoint.getAttachPoints(annotationSymbol.maskedPoints));
        }
    }
}
