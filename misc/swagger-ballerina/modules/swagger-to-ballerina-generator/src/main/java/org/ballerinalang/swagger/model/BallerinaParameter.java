/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.swagger.model;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

import java.util.Map;

/**
 * Wraps the {@link Parameter} from swagger models for easier templating.
 *
 * @since 0.967.0
 */
public class BallerinaParameter implements BallerinaSwaggerObject<BallerinaParameter, Parameter> {
    private String name;
    private String in;
    private String description;
    private Boolean required;
    private Boolean deprecated;
    private Boolean allowEmptyValue;
    private String ref;
    private Parameter.StyleEnum style;
    private Boolean explode;
    private Boolean allowReserved;
    private BallerinaSchema schema;
    private Map<String, Example> examples;
    private Object example;
    private Content content;
    private Map<String, Object> extensions;

    @Override
    public BallerinaParameter buildContext(Parameter parameter) throws BallerinaOpenApiException {
        return buildContext(parameter, null);
    }

    @Override
    public BallerinaParameter buildContext(Parameter parameter, OpenAPI openAPI) throws BallerinaOpenApiException {
        this.name = parameter.getName();
        this.in = parameter.getIn();
        this.description = parameter.getDescription();
        this.required = parameter.getRequired();
        this.deprecated = parameter.getDeprecated();
        this.allowEmptyValue = parameter.getAllowEmptyValue();
        this.ref = parameter.get$ref();
        this.style = parameter.getStyle();
        this.explode = parameter.getExplode();
        this.allowReserved = parameter.getAllowReserved();
        this.examples = parameter.getExamples();
        this.example = parameter.getExample();
        this.content = parameter.getContent();
        this.extensions = parameter.getExtensions();
        this.schema = new BallerinaSchema().buildContext(parameter.getSchema(), openAPI);

        return this;
    }

    @Override
    public BallerinaParameter getDefaultValue() {
        return null;
    }

    public String getName() {
        return name;
    }

    public String getIn() {
        return in;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getRequired() {
        return required;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public Boolean getAllowEmptyValue() {
        return allowEmptyValue;
    }

    public String getRef() {
        return ref;
    }

    public Parameter.StyleEnum getStyle() {
        return style;
    }

    public Boolean getExplode() {
        return explode;
    }

    public Boolean getAllowReserved() {
        return allowReserved;
    }

    public BallerinaSchema getSchema() {
        return schema;
    }

    public Map<String, Example> getExamples() {
        return examples;
    }

    public Object getExample() {
        return example;
    }

    public Content getContent() {
        return content;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }
}
