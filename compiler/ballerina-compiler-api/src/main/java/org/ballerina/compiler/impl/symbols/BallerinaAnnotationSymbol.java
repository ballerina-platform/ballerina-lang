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
package org.ballerina.compiler.impl.symbols;

import org.ballerina.compiler.api.symbols.AnnotationAttachPoint;
import org.ballerina.compiler.api.symbols.AnnotationSymbol;
import org.ballerina.compiler.api.symbols.Qualifier;
import org.ballerina.compiler.api.symbols.SymbolKind;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represent Annotation Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaAnnotationSymbol extends BallerinaSymbol implements AnnotationSymbol {

    private final List<Qualifier> qualifiers;
    private final BallerinaTypeDescriptor typeDescriptor;
    private final List<AnnotationAttachPoint> attachPoints;

    private BallerinaAnnotationSymbol(String name, PackageID moduleID, List<Qualifier> qualifiers,
            BallerinaTypeDescriptor typeDescriptor, List<AnnotationAttachPoint> attachPoints, BSymbol bSymbol) {
        super(name, moduleID, SymbolKind.ANNOTATION, bSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
        this.attachPoints = Collections.unmodifiableList(attachPoints);
    }

    /**
     * Get the qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    /**
     * Get the type descriptor.
     *
     * @return {@link Optional} type descriptor of the annotation
     */
    @Override
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    /**
     * Get the attached points.
     *
     * @return {@link List} of attached points
     */
    @Override
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
        private List<AnnotationAttachPoint> attachPoints;

        public AnnotationSymbolBuilder(String name, PackageID moduleID, BAnnotationSymbol annotationSymbol) {
            super(name, moduleID, SymbolKind.ANNOTATION, annotationSymbol);
            withAnnotationSymbol(annotationSymbol);
        }

        public BallerinaAnnotationSymbol build() {
            return new BallerinaAnnotationSymbol(this.name, this.moduleID, this.qualifiers, this.typeDescriptor,
                    this.attachPoints, this.bSymbol);
        }

        /**
         * Set the attach points from the annotation symbol.
         *
         * @param annotationSymbol annotation symbol to evaluate
         */
        private void withAnnotationSymbol(BAnnotationSymbol annotationSymbol) {
            this.attachPoints = getAttachPoints(annotationSymbol.maskedPoints);
        }

        public AnnotationSymbolBuilder setQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public AnnotationSymbolBuilder withTypeDescriptor(BallerinaTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        /**
         * Get the attached points from the masked points.
         *
         * @param maskedPoints masked points
         * @return {@link List} of annotation attach points
         */
        public static List<AnnotationAttachPoint> getAttachPoints(int maskedPoints) {
            List<AnnotationAttachPoint> annotationAttachPoints = new ArrayList<>();
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)) {
                annotationAttachPoints.add(AnnotationAttachPoint.TYPE);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT)) {
                annotationAttachPoints.add(AnnotationAttachPoint.OBJECT);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)) {
                annotationAttachPoints.add(AnnotationAttachPoint.FUNCTION);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT_METHOD)) {
                annotationAttachPoints.add(AnnotationAttachPoint.OBJECT_METHOD);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RESOURCE)) {
                annotationAttachPoints.add(AnnotationAttachPoint.RESOURCE);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.PARAMETER)) {
                annotationAttachPoints.add(AnnotationAttachPoint.PARAMETER);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RETURN)) {
                annotationAttachPoints.add(AnnotationAttachPoint.RETURN);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.SERVICE)) {
                annotationAttachPoints.add(AnnotationAttachPoint.SERVICE);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.LISTENER)) {
                annotationAttachPoints.add(AnnotationAttachPoint.LISTENER);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.ANNOTATION)) {
                annotationAttachPoints.add(AnnotationAttachPoint.ANNOTATION);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.EXTERNAL)) {
                annotationAttachPoints.add(AnnotationAttachPoint.EXTERNAL);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.VAR)) {
                annotationAttachPoints.add(AnnotationAttachPoint.VAR);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.CONST)) {
                annotationAttachPoints.add(AnnotationAttachPoint.CONST);
            }
            if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.WORKER)) {
                annotationAttachPoints.add(AnnotationAttachPoint.WORKER);
            }

            return annotationAttachPoints;
        }
    }
}
