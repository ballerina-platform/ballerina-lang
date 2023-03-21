/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.model;

import io.ballerina.architecturemodelgenerator.model.service.DisplayAnnotation;
import io.ballerina.architecturemodelgenerator.model.service.FunctionParameter;
import io.ballerina.architecturemodelgenerator.model.service.Interaction;

import java.util.List;

/**
 * Represents EntryPoint related information.
 *
 * @since 2201.2.2
 */
public class EntryPoint extends ModelElement {

    private final String id;
    private final List<FunctionParameter> parameters;
    private final List<String> returns;
    private final List<Interaction> interactions;
    private final DisplayAnnotation annotation;

    public EntryPoint(String id, List<FunctionParameter> parameters, List<String> returns,
                      List<Interaction> interactions, DisplayAnnotation annotation, ElementLocation elementLocation) {
        super(elementLocation);
        this.id = id;
        this.parameters = parameters;
        this.returns = returns;
        this.annotation = annotation;
        this.interactions = interactions;
    }

    public String getId() {
        return id;
    }

    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public List<String> getReturns() {
        return returns;
    }

    public List<Interaction> getInteractions() {
        return interactions;
    }

    public DisplayAnnotation getAnnotation() {
        return annotation;
    }
}
