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
                                      List<AnnotationAttachPoint> attachPoints) {
        super(name, moduleID, accessModifiers, BallerinaSymbolKind.ANNOTATION, typeDescriptor);
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
        
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public AnnotationSymbolBuilder(String name, PackageID moduleID) {
            super(name, moduleID);
        }

        public BallerinaAnnotationSymbol build() {
            return new BallerinaAnnotationSymbol(this.name,
                    this.moduleID,
                    this.accessModifiers,
                    this.typeDescriptor,
                    this.attachPoints);
        }

        /**
         * Set the attach points from the annotation symbol.
         * 
         * @param annotationSymbol annotation symbol to evaluate
         * @return {@link AnnotationSymbolBuilder} annotation builder 
         */
        public AnnotationSymbolBuilder withAnnotationSymbol(BAnnotationSymbol annotationSymbol) {
            this.attachPoints.addAll(AnnotationAttachPoint.getAttachPoints(annotationSymbol.maskedPoints));
            return this;
        }
    }
}
