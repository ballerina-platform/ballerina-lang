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
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.utils.GeneratorConstants;

import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wraps {@link RequestBody} to build a easily accessible RequestBody object model for templates.
 */
public class BallerinaRequestBody implements BallerinaSwaggerObject<BallerinaRequestBody, RequestBody> {
    private String description;
    private Set<Map.Entry<String, BallerinaMediaType>> content;
    private Boolean required;
    private Map<String, Object> extensions;
    private String ref;
    private BallerinaMediaType selectedMedia; // selected specific media type in multi content scenario

    @Override
    public BallerinaRequestBody buildContext(RequestBody body) throws BallerinaOpenApiException {
        return buildContext(body, null);
    }

    @Override
    public BallerinaRequestBody buildContext(RequestBody body, OpenAPI openAPI) throws BallerinaOpenApiException {
        if (body == null || openAPI == null) {
            return getDefaultValue();
        }
        if (body.getContent() == null) {
            throw new BallerinaOpenApiException("RequestBody content cannot be null");
        }

        // If reference type request body is provided, extract the reference
        if (!StringUtils.isEmpty(body.get$ref())) {
            String refType = getReferenceType(body.get$ref());
            body = openAPI.getComponents().getRequestBodies().get(refType);
        }

        this.description = body.getDescription();
        this.required = body.getRequired();
        this.extensions = body.getExtensions();
        this.ref = body.get$ref();
        this.content = new LinkedHashSet<>();

        int i = 0;
        for (Map.Entry<String, MediaType> m : body.getContent().entrySet()) {
            BallerinaMediaType bMedia = new BallerinaMediaType().buildContext(m.getValue(), openAPI);
            bMedia.setMediaType(m.getKey());
            String bType = getRequestBType(m.getKey());

            // Update the response body schema type to a matching ballerina type
            // bType for json media types will not be changes since bType and json is interchangeable
            if (bType != null) {
                bMedia.getSchema().setType(bType);
            }
            AbstractMap.Entry<String, BallerinaMediaType> entry = new AbstractMap.SimpleEntry<>(m.getKey(), bMedia);

            // keep first mediaType as selected media type
            if (i == 0) {
                this.selectedMedia = bMedia;
            }
            content.add(entry);
            i++;
        }

        return this;
    }

    /**
     * Retrieve the matching ballerina type for a http media type.
     *
     * @param mType http media type
     * @return ballerina type for {@code mType}
     */
    private String getRequestBType(String mType) {
        String type = null;

        // support all xml types
        if (mType == null) {
            type = null;
        } else if (javax.ws.rs.core.MediaType.TEXT_PLAIN.equals(mType)) {
            type = "string";
        } else if (javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM.equals(mType)) {
            type = "blob";
        } else if (mType.endsWith("/json") || mType.endsWith("+json")) {
            // null specifies that the type returned by this method should not be used
            type = null;
        } else if (mType.endsWith("/xml") || mType.endsWith("+xml")) {
            type = "xml";
        } else if (mType.startsWith("multipart/")) {
            type = "mime:Entity[]";
        }

        return type;
    }

    @Override
    public BallerinaRequestBody getDefaultValue() {
        return null;
    }

    private String getReferenceType(String refPath) {
        // null check on refPath is not required since swagger parser always make sure this is not null
        return refPath.substring(refPath.lastIndexOf(GeneratorConstants.OAS_PATH_SEPARATOR) + 1);
    }

    public String getDescription() {
        return description;
    }

    public Set<Map.Entry<String, BallerinaMediaType>> getContent() {
        return content;
    }

    public Boolean getRequired() {
        return required;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public String getRef() {
        return ref;
    }

    public BallerinaMediaType getSelectedMedia() {
        return selectedMedia;
    }
}
