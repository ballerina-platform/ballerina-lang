/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.query.api.util;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;

import java.util.List;

/**
 * Created by suho on 8/8/14.
 */
public class AnnotationHelper {

    // TODO: 1/28/17 update helper methods to work with nested annotations.
    public static Annotation getAnnotation(String annotationName, List<Annotation> annotationList) {
        Annotation annotation = null;
        for (Annotation aAnnotation : annotationList) {
            if (annotationName.equalsIgnoreCase(aAnnotation.getName())) {
                if (annotation == null) {
                    annotation = aAnnotation;
                } else {
                    throw new DuplicateAnnotationException("Annotation @" + annotationName + " is defined twice");
                }
            }
        }
        return annotation;
    }

    // TODO: 1/28/17 update helper methods to work with nested annotations.
    public static Element getAnnotationElement(String annotationName, String elementName, List<Annotation>
            annotationList) {
        Annotation annotation = getAnnotation(annotationName, annotationList);
        if (annotation != null) {
            Element element = null;
            for (Element aElement : annotation.getElements()) {
                if (elementName == null) {
                    if (aElement.getKey() == null) {

                        if (element == null) {
                            element = aElement;
                        } else {
                            throw new DuplicateAnnotationException("Annotation element @" + annotationName + "(...) " +
                                    "is defined twice");
                        }
                    }
                } else {
                    if (elementName.equalsIgnoreCase(aElement.getKey())) {

                        if (element == null) {
                            element = aElement;
                        } else {
                            throw new DuplicateAnnotationException("Annotation element @" + annotationName + "(" +
                                    elementName + "=...) is defined twice");
                        }
                    }
                }

            }
            return element;
        }
        return null;
    }
}
