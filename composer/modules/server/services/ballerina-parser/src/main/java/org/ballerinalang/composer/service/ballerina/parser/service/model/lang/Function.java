/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.ballerina.parser.service.model.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Function.
 */

public class Function {
    private String name = null;

    private Integer packageId = null;

    private String description = null;

    private Integer boundObjectId = null;

    private List<AnnotationAttachment> annotations = new ArrayList<>();

    private List<Parameter> parameters = new ArrayList<Parameter>();

    private List<Parameter> returnParams = new ArrayList<Parameter>();

    private String receiverType = null;

    private Boolean isPublic = false;

    private String fileName = null;

    public Function name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name.
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Function description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description.
     *
     * @return description
     **/
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Function annotations(List<AnnotationAttachment> annotations) {
        this.annotations = annotations;
        return this;
    }

    public Function addAnnotationsItem(AnnotationAttachment annotationsItem) {
        this.annotations.add(annotationsItem);
        return this;
    }

    /**
     * Get annotations.
     *
     * @return annotations
     **/
    public List<AnnotationAttachment> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationAttachment> annotations) {
        this.annotations = annotations;
    }

    public Function parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public Function addParametersItem(Parameter parametersItem) {
        this.parameters.add(parametersItem);
        return this;
    }

    /**
     * Get parameters.
     *
     * @return parameters
     **/
    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Function returnParams(List<Parameter> returnParams) {
        this.returnParams = returnParams;
        return this;
    }

    public Function addReturnParamsItem(Parameter returnParamsItem) {
        this.returnParams.add(returnParamsItem);
        return this;
    }

    /**
     * Get returnParams.
     *
     * @return returnParams
     **/
    public List<Parameter> getReturnParams() {
        return returnParams;
    }

    public void setReturnParams(List<Parameter> returnParams) {
        this.returnParams = returnParams;
    }


    /**
     * Get ReceiverType.
     *
     * @return receiverType
     */
    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    /**
     * Returns true if the function is public else false.
     *
     * @return {@link Boolean} whether the function is pubic or not
     */
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Function function = (Function) o;
        return Objects.equals(this.name, function.name) &&
                Objects.equals(this.description, function.description) &&
                Objects.equals(this.annotations, function.annotations) &&
                Objects.equals(this.parameters, function.parameters) &&
                Objects.equals(this.returnParams, function.returnParams) &&
                Objects.equals(this.receiverType, function.receiverType) &&
                Objects.equals(this.isPublic, function.isPublic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, annotations, parameters, returnParams, receiverType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Function {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    annotations: ").append(toIndentedString(annotations)).append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
        sb.append("    returnParams: ").append(toIndentedString(returnParams)).append("\n");
        sb.append("    receiverType: ").append(toIndentedString(receiverType)).append("\n");
        sb.append("    isPublic: ").append(toIndentedString(isPublic)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

