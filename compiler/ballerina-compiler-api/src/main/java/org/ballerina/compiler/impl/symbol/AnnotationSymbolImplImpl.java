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
package org.ballerina.compiler.impl.symbol;

import org.ballerina.compiler.api.element.AnnotationAttachPoint;
import org.ballerina.compiler.api.symbol.AnnotationSymbol;
import org.ballerina.compiler.api.symbol.BallerinaSymbolKind;
import org.ballerina.compiler.api.symbol.Qualifier;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represent Annotation Symbol.
 *
 * @since 2.0.0
 */
public class AnnotationSymbolImplImpl extends BallerinaSymbolImpl implements AnnotationSymbol {

    private final List<Qualifier> qualifiers;

    private final BallerinaTypeDescriptor typeDescriptor;

    private final List<AnnotationAttachPoint> attachPoints;

    private AnnotationSymbolImplImpl(String name,
                                     PackageID moduleID,
                                     List<Qualifier> qualifiers,
                                     BallerinaTypeDescriptor typeDescriptor,
                                     List<AnnotationAttachPoint> attachPoints,
                                     BSymbol bSymbol) {
        super(name, moduleID, BallerinaSymbolKind.ANNOTATION, bSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
        this.attachPoints = Collections.unmodifiableList(attachPoints);
    }

    /**
     * Get the qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    /**
     * Get the type descriptor.
     *
     * @return {@link Optional} type descriptor of the annotation
     */
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    /**
     * Get the attached points.
     *
     * @return {@link List} of attached points
     */
    public List<AnnotationAttachPoint> attachPoints() {
        return attachPoints;
    }

    /**
     * Represents Ballerina Annotation Symbol Builder.
     *
     * @since 1.3.0
     */
    public static class AnnotationSymbolBuilder extends SymbolBuilder<AnnotationSymbolBuilder> {

        private List<Qualifier> qualifiers = new ArrayList<>();

        private BallerinaTypeDescriptor typeDescriptor;

        private List<AnnotationAttachPoint> attachPoints = new ArrayList<>();

        public AnnotationSymbolBuilder(String name, PackageID moduleID, BAnnotationSymbol annotationSymbol) {
            super(name, moduleID, BallerinaSymbolKind.ANNOTATION, annotationSymbol);
            withAnnotationSymbol(annotationSymbol);
        }

        public AnnotationSymbolImplImpl build() {
            return new AnnotationSymbolImplImpl(this.name,
                    this.moduleID,
                    this.qualifiers,
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

        public AnnotationSymbolBuilder setQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public AnnotationSymbolBuilder withTypeDescriptor(BallerinaTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

    }
}
