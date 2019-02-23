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
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

import java.util.Map;

/**
 * Wraps {@link MediaType} to build a MediaType object model for ballerina templates.
 */
public class BallerinaMediaType implements BallerinaSwaggerObject<BallerinaMediaType, MediaType> {
    private BallerinaSchema schema;
    private Map<String, Example> examples;
    private Object example;
    private Map<String, Encoding> encoding;
    private Map<String, Object> extensions;
    private String mediaType;

    @Override
    public BallerinaMediaType buildContext(MediaType mediaType) throws BallerinaOpenApiException {
        return buildContext(mediaType, null);
    }

    @Override
    public BallerinaMediaType buildContext(MediaType mediaType, OpenAPI openAPI) throws BallerinaOpenApiException {
        this.example = mediaType.getExample();
        this.extensions = mediaType.getExtensions();
        this.encoding = mediaType.getEncoding();
        this.examples = mediaType.getExamples();
        this.schema = new BallerinaSchema().buildContext(mediaType.getSchema(), openAPI);

        return this;
    }

    @Override
    public BallerinaMediaType getDefaultValue() {
        return new BallerinaMediaType();
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

    public Map<String, Encoding> getEncoding() {
        return encoding;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
