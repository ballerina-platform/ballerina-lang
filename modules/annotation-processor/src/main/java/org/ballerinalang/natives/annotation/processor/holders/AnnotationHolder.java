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
package org.ballerinalang.natives.annotation.processor.holders;

import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds a {@link BallerinaAnnotation}
 */
public class AnnotationHolder {
    
    private BallerinaAnnotation annotation;
    private String name;
    
    public AnnotationHolder(BallerinaAnnotation annot) {
        this.annotation = annot;
        this.name = annot.annotationName();
    }

    public String getName() {
        return name;
    }
    
    public List<Attribute> getAttributes() {
        return Arrays.asList(annotation.attributes());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + annotation.annotationName() + " (");
        List<Attribute> attributes = getAttributes();
        sb.append(attributes.stream().map(p -> p.name() + "=\"" + p.value() + "\" ").collect(Collectors.joining(",")));
        sb.append(")");
        return sb.toString();
    }
}
