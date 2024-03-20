/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.docgen.generator.model;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Represent documentation for a Function.
 */
public class Function extends Construct {
    @Expose
    public String accessor = "";
    @Expose
    public String resourcePath = "";
    @Expose
    public boolean isIsolated;
    @Expose
    public boolean isRemote;
    @Expose
    public boolean isResource;
    @Expose
    public boolean isExtern;
    @Expose
    public List<DefaultableVariable> parameters;
    @Expose
    public List<Variable> returnParameters;
    @Expose
    List<AnnotationAttachment> annotationAttachments;

    public Function(String name, String description, List<String> descriptionSections, FunctionKind functionKind,
                    boolean isExtern, boolean isDeprecated, boolean isIsolated, List<DefaultableVariable> parameters,
                    List<Variable> returnParameters, List<AnnotationAttachment> annotationAttachments) {
        super(name, description, descriptionSections, isDeprecated);
        this.isRemote = checkRemote(functionKind);
        this.isResource = checkResource(functionKind);
        this.isExtern = isExtern;
        this.parameters = parameters;
        this.returnParameters = returnParameters;
        this.isIsolated = isIsolated;
        this.annotationAttachments = annotationAttachments;
    }

    public Function(String name, String accessor, String resourcePath, String description,
                    List<String> descriptionSections, FunctionKind functionKind, boolean isExtern,
                    boolean isDeprecated, boolean isIsolated, List<DefaultableVariable> parameters,
                    List<Variable> returnParameters, List<AnnotationAttachment> annotationAttachments) {
        super(name, description, descriptionSections, isDeprecated);
        this.accessor = accessor;
        this.resourcePath = resourcePath;
        this.isRemote = checkRemote(functionKind);
        this.isResource = checkResource(functionKind);
        this.isExtern = isExtern;
        this.parameters = parameters;
        this.returnParameters = returnParameters;
        this.isIsolated = isIsolated;
        this.annotationAttachments = annotationAttachments;
    }

    public Function(String name, String description, List<String> descriptionSections, FunctionKind functionKind,
                    boolean isExtern, boolean isDeprecated, boolean isIsolated, List<DefaultableVariable> parameters,
                    List<Variable> returnParameters) {
        super(name, description, descriptionSections, isDeprecated);
        this.isRemote = checkRemote(functionKind);
        this.isResource = checkResource(functionKind);
        this.isExtern = isExtern;
        this.parameters = parameters;
        this.returnParameters = returnParameters;
        this.isIsolated = isIsolated;
    }

    public Function(String name, String accessor, String resourcePath, String description,
                    List<String> descriptionSections, FunctionKind functionKind, boolean isExtern,
                    boolean isDeprecated, boolean isIsolated, List<DefaultableVariable> parameters,
                    List<Variable> returnParameters) {
        super(name, description, descriptionSections, isDeprecated);
        this.accessor = accessor;
        this.resourcePath = resourcePath;
        this.isRemote = checkRemote(functionKind);
        this.isResource = checkResource(functionKind);
        this.isExtern = isExtern;
        this.parameters = parameters;
        this.returnParameters = returnParameters;
        this.isIsolated = isIsolated;
    }

    private boolean checkRemote(FunctionKind functionKind) {
        return functionKind == FunctionKind.REMOTE;
    }

    private boolean checkResource(FunctionKind functionKind) {
        return functionKind == FunctionKind.RESOURCE;
    }
}
