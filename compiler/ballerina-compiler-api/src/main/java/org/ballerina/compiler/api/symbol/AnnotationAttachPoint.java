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
package org.ballerina.compiler.api.symbol;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the annotation attach points.
 * 
 * @since 2.0.0
 */
public enum AnnotationAttachPoint {
    TYPE,
    OBJECT,
    FUNCTION,
    OBJECT_METHOD,
    RESOURCE,
    PARAMETER,
    RETURN,
    SERVICE,
    FIELD,
    OBJECT_FIELD,
    RECORD_FIELD,
    LISTENER,
    ANNOTATION,
    EXTERNAL,
    VAR,
    CONST,
    WORKER;

    /**
     * Get the attached points from the masked points.
     * 
     * @param maskedPoints masked points
     * @return {@link List} of annotation attach points
     */
    public static List<AnnotationAttachPoint> getAttachPoints(int maskedPoints) {
        List<AnnotationAttachPoint> annotationAttachPoints = new ArrayList<>();
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.TYPE)) {
            annotationAttachPoints.add(TYPE);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT)) {
            annotationAttachPoints.add(OBJECT);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.FUNCTION)) {
            annotationAttachPoints.add(FUNCTION);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.OBJECT_METHOD)) {
            annotationAttachPoints.add(OBJECT_METHOD);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RESOURCE)) {
            annotationAttachPoints.add(RESOURCE);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.PARAMETER)) {
            annotationAttachPoints.add(PARAMETER);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.RETURN)) {
            annotationAttachPoints.add(RETURN);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.SERVICE)) {
            annotationAttachPoints.add(SERVICE);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.LISTENER)) {
            annotationAttachPoints.add(LISTENER);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.ANNOTATION)) {
            annotationAttachPoints.add(ANNOTATION);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.EXTERNAL)) {
            annotationAttachPoints.add(EXTERNAL);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.VAR)) {
            annotationAttachPoints.add(VAR);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.CONST)) {
            annotationAttachPoints.add(CONST);
        }
        if (Symbols.isAttachPointPresent(maskedPoints, AttachPoints.WORKER)) {
            annotationAttachPoints.add(WORKER);
        }
        
        return annotationAttachPoints;
    } 
}
