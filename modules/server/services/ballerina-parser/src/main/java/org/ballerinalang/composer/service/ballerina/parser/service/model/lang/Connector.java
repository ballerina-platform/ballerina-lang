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
 * Connector.
 */
public class Connector {
    private String name = null;

    private String fileName = null;

    private List<AnnotationAttachment> annotations = new ArrayList<>();

    private List<Parameter> returnParameters = new ArrayList<>();

    private List<Parameter> parameters = new ArrayList<Parameter>();

    private List<Action> actions = new ArrayList<Action>();

    public Connector name(String name) {
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

    public Connector annotations(List<AnnotationAttachment> annotations) {
        this.annotations = annotations;
        return this;
    }

    public Connector addAnnotationsItem(AnnotationAttachment annotationsItem) {
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

    public Connector parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public Connector addParametersItem(Parameter parametersItem) {
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

    public Connector actions(List<Action> actions) {
        this.actions = actions;
        return this;
    }

    public Connector addActionsItem(Action actionsItem) {
        this.actions.add(actionsItem);
        return this;
    }

    /**
     * Get actions.
     *
     * @return actions
     **/
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Parameter> getReturnParameters() {
        return returnParameters;
    }

    public void setReturnParameters(List<Parameter> returnParameters) {
        this.returnParameters = returnParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Connector connector = (Connector) o;
        return Objects.equals(this.name, connector.name) &&
                Objects.equals(this.annotations, connector.annotations) &&
                Objects.equals(this.parameters, connector.parameters) &&
                Objects.equals(this.actions, connector.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, annotations, parameters, actions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Connector {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    annotations: ").append(toIndentedString(annotations)).append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
        sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
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

