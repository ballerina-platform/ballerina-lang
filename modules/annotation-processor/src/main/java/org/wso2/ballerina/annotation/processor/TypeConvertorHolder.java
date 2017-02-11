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
package org.wso2.ballerina.annotation.processor;

import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeConvertor;

import java.util.List;

/**
 * DTO to hold Ballerina type convertor annotation
 */
public class TypeConvertorHolder {
    
    private BallerinaTypeConvertor typeConvertor;
    private String typeConvertorClassName;
    private List<AnnotationHolder> annotations;
    
    public TypeConvertorHolder(BallerinaTypeConvertor balTypeConvertor, String className, 
            BallerinaAnnotation[] annotations) {
        this.typeConvertor = balTypeConvertor;
        this.typeConvertorClassName = className;
        this.annotations = Utils.getAnnotations(annotations);;
    }
    
    public String getClassName() {
        return typeConvertorClassName;
    }
    
    public BallerinaTypeConvertor getBalTypeConvertor() {
        return typeConvertor;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Utils.appendAnnotationStrings(sb, annotations);
        sb.append("native typeconvertor ").append(typeConvertor.typeConverterName());
        Utils.getInputParams(typeConvertor.args(), sb);
        Utils.getReturnParams(typeConvertor.returnType(), sb);
        sb.append(";");
        return sb.toString();
    }
}
