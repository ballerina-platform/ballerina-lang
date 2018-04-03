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

package org.ballerinalang.swagger.code.generator;

import io.swagger.models.ExternalDocs;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.swagger.code.generator.util.SwaggerConstants;
import org.ballerinalang.swagger.code.generator.util.SwaggerUtils;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ParamAnnAttachmentInfo;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamAnnotationAttributeInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class will do resource mapping from ballerina to swagger.
 */
class SwaggerResourceMapper {

    private final Swagger swaggerDefinition;

    SwaggerResourceMapper(Swagger swagger) {
        this.swaggerDefinition = swagger;
    }

    /**
     * This method will convert ballerina resource to swagger path objects.
     *
     * @param resources Resource array to be convert.
     */
    void mapResourcesToPathPaths(ResourceInfo[] resources) {
        Map<String, Path> map = new HashMap<>();
        for (ResourceInfo subResource : resources) {
            OperationAdaptor operationAdaptor = this.convertResourceToOperation(subResource);
            Path path = map.get(operationAdaptor.getPath());
            //TODO this check need to be improve to avoid repetition checks and http head support need to add.
            if (path == null) {
                path = new Path();
                map.put(operationAdaptor.getPath(), path);
            }
            String httpOperation = operationAdaptor.getHttpOperation();
            Operation operation = operationAdaptor.getOperation();
            switch (httpOperation) {
                case HttpConstants.ANNOTATION_METHOD_GET:
                    path.get(operation);
                    break;
                case HttpConstants.ANNOTATION_METHOD_PUT:
                    path.put(operation);
                    break;
                case HttpConstants.ANNOTATION_METHOD_POST:
                    path.post(operation);
                    break;
                case HttpConstants.ANNOTATION_METHOD_DELETE:
                    path.delete(operation);
                    break;
                case HttpConstants.ANNOTATION_METHOD_OPTIONS:
                    path.options(operation);
                    break;
                case HttpConstants.ANNOTATION_METHOD_PATCH:
                    path.patch(operation);
                    break;
                case "HEAD":
                    path.head(operation);
                    break;
                default:
                    break;
            }
        }
        this.swaggerDefinition.setPaths(map);
    }


    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor
     *
     * @param resource Resource array to be convert.
     * @return @OperationAdaptor of string and swagger path objects.
     */
    private OperationAdaptor convertResourceToOperation(ResourceInfo resource) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            // Setting default values.
            op.setHttpOperation(HttpConstants.HTTP_METHOD_GET);
            op.setPath('/' + resource.getName());
            Response response = new Response()
                    .description("Successful")
                    .example("application/json", "Ok");
            op.getOperation().response(200, response);
            op.getOperation().setOperationId(resource.getName());
            op.getOperation().setParameters(null);

            // Parsing annotations.
            this.parsePathAnnotationAttachment(resource, op);
            this.parseHttpMethodAnnotationAttachment(resource, op);
            this.parseResourceConfigAnnotationAttachment(resource, op.getOperation());
            this.parseConsumesAnnotationAttachment(resource, op.getOperation());
            this.parseProducesAnnotationAttachment(resource, op.getOperation());
            this.parseResourceInfoAnnotationAttachment(resource, op.getOperation());
            this.addResourceParameters(resource, op);
            this.parseParametersInfoAnnotationAttachment(resource, op.getOperation());
            this.parseResponsesAnnotationAttachment(resource, op.getOperation());

        }
        return op;

    }

    /**
     * Parses the 'Responses' annotation attachment and build swagger operation.
     *
     * @param resource The ballerina resource definition.
     * @param op       The swagger operation.
     */
    private void parseResponsesAnnotationAttachment(ResourceInfo resource, Operation op) {
        AnnAttachmentInfo responsesAnnotationAttachment =
                resource.getAnnotationAttachmentInfo(SwaggerConstants.SWAGGER_PACKAGE_PATH, "Responses");
        if (null != responsesAnnotationAttachment) {
            Map<String, AnnAttributeValue> responsesAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                    (responsesAnnotationAttachment);
            if (null != responsesAnnAttributeValueMap.get("value")) {
                AnnAttributeValue[] responsesValues = responsesAnnAttributeValueMap.get("value")
                        .getAttributeValueArray();
                if (responsesValues.length > 0) {
                    Map<String, Response> responses = new HashMap<>();
                    for (AnnAttributeValue responsesValue : responsesValues) {
                        AnnAttachmentInfo responseAnnotationAttachment = responsesValue.getAnnotationAttachmentValue();
                        Map<String, AnnAttributeValue> responseAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                                (responseAnnotationAttachment);
                        if (null != responseAnnAttributeValueMap.get("code")) {
                            String code = responseAnnAttributeValueMap.get("code").getStringValue();
                            Response response = new Response();
                            if (null != responseAnnAttributeValueMap.get("description")) {
                                response.setDescription(responseAnnAttributeValueMap.get("description")
                                        .getStringValue());
                            }
                            // TODO: Parse 'response' attribute for $.paths./resource-path.responses[*]["code"].schema
                            this.createHeadersModel(responseAnnAttributeValueMap.get("headers"), response);
                            responses.put(code, response);
                        }
                    }
                    op.setResponses(responses);
                }
            }
        }
    }

    /**
     * Creates headers definitions for swagger response.
     *
     * @param annotationAttributeValue The annotation attribute value which has the headers.
     * @param response                 The swagger response.
     */
    private void createHeadersModel(AnnAttributeValue annotationAttributeValue, Response response) {
        if (null != annotationAttributeValue) {
            AnnAttributeValue[] headersValueArray = annotationAttributeValue.getAttributeValueArray();
            for (AnnAttributeValue headersValue : headersValueArray) {
                AnnAttachmentInfo headerAnnotationAttachment = headersValue.getAnnotationAttachmentValue();
                Map<String, AnnAttributeValue> headerAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                        (headerAnnotationAttachment);
                Map<String, Property> headers = new HashMap<>();
                if (null != headerAnnAttributeValueMap.get("name") &&
                        null != headerAnnAttributeValueMap.get("headerType")) {
                    String headerName = headerAnnAttributeValueMap.get("name").getStringValue();
                    String type = headerAnnAttributeValueMap.get("headerType").getStringValue();
                    Property property = null;
                    if ("string".equals(type)) {
                        property = new StringProperty();
                    } else if ("number".equals(type) || "integer".equals(type)) {
                        property = new IntegerProperty();
                    } else if ("boolean".equals(type)) {
                        property = new BooleanProperty();
                    } else if ("array".equals(type)) {
                        property = new ArrayProperty();
                    }
                    if (null != property) {
                        if (null != headerAnnAttributeValueMap.get("description")) {
                            property.setDescription(headerAnnAttributeValueMap.get("description").getStringValue());
                        }
                        headers.put(headerName, property);
                    }
                }
                response.setHeaders(headers);
            }
        }
    }

    /**
     * Creates parameters in the swagger operation using the parameters in the ballerina resource definition.
     *
     * @param resource         The ballerina resource definition.
     * @param operationAdaptor The swagger operation.
     */
    private void addResourceParameters(ResourceInfo resource, OperationAdaptor operationAdaptor) {
        if (!"get".equalsIgnoreCase(operationAdaptor.getHttpOperation())) {

            // Creating message body - required.
            ModelImpl messageModel = new ModelImpl();
            messageModel.setType("object");
            Map<String, Model> definitions = new HashMap<>();
            if (!definitions.containsKey("Message")) {
                definitions.put("Message", messageModel);
                this.swaggerDefinition.setDefinitions(definitions);
            }

            // Creating "Message m" parameter
            BodyParameter messageParameter = new BodyParameter();
            messageParameter.setName(resource.getParamNames()[0]);
            RefModel refModel = new RefModel();
            refModel.setReference("Message");
            messageParameter.setSchema(refModel);
            operationAdaptor.getOperation().addParameter(messageParameter);
        }

        // Creating query params and path params
        AttributeInfo attributeInfo = resource.getAttributeInfo(AttributeInfo.Kind.PARAMETER_ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo instanceof ParamAnnotationAttributeInfo) {
            ParamAnnotationAttributeInfo paramAttributeInfo = (ParamAnnotationAttributeInfo) resource.getAttributeInfo
                    (AttributeInfo.Kind.PARAMETER_ANNOTATIONS_ATTRIBUTE);
            ParamAnnAttachmentInfo[] attachmentInfoArray = paramAttributeInfo.getAttachmentInfoArray();
            for (ParamAnnAttachmentInfo paramAnnAttachmentInfo : attachmentInfoArray) {
                if (paramAnnAttachmentInfo.getAnnAttachmentInfos().length > 0) {
                    AnnAttachmentInfo annAttachmentInfo = paramAnnAttachmentInfo.getAnnAttachmentInfos()[0];
                    Map<String, AnnAttributeValue> paramAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                            (annAttachmentInfo);
                    if (paramAnnAttributeValueMap.size() == 1 && null != paramAnnAttributeValueMap.get("value")) {
                        // Add query parameter
                        if (annAttachmentInfo.getName().equalsIgnoreCase("QueryParam")) {
                            QueryParameter queryParameter = new QueryParameter();
                            // Set in value.
                            queryParameter.setIn("query");
                            // Set parameter name
                            String parameterName = paramAnnAttributeValueMap.get("value").getStringValue();
                            if ((parameterName == null) || parameterName.isEmpty()) {
                                parameterName = resource.getParamNames()[paramAnnAttachmentInfo.getParamIdex()];
                            }
                            queryParameter.setName(parameterName);
                            // Note: 'description' to be added using annotations, hence skipped here.
                            // Setting false to required(as per swagger spec). This can be overridden while parsing
                            // annotations.

                            queryParameter.required(false);
                            // Note: 'allowEmptyValue' to be added using annotations, hence skipped here.
                            // Set type
                            String paramType = resource.getParamTypes()[paramAnnAttachmentInfo.getParamIdex()]
                                    .getName();
                            if ("int".equals(paramType)) {
                                queryParameter.setType("integer");
                            } else {
                                queryParameter.setType(paramType);
                            }
                            // Note: 'format' to be added using annotations, hence skipped here.

                            operationAdaptor.getOperation().addParameter(queryParameter);
                        }
                        if (annAttachmentInfo.getName().equalsIgnoreCase("PathParam")) {
                            PathParameter pathParameter = new PathParameter();
                            // Set in value
                            pathParameter.setIn("path");
                            // Set parameter name
                            String parameterName = paramAnnAttributeValueMap.get("value").getStringValue();
                            if ((parameterName == null) || parameterName.isEmpty()) {
                                parameterName = resource.getParamNames()[paramAnnAttachmentInfo.getParamIdex()];
                            }
                            pathParameter.setName(parameterName);
                            // Note: 'description' to be added using annotations, hence skipped here.
                            // Note: 'allowEmptyValue' to be added using annotations, hence skipped here.
                            // Set type
                            String paramType = resource.getParamTypes()[paramAnnAttachmentInfo.getParamIdex()]
                                    .getName();
                            if ("int".equals(paramType)) {
                                pathParameter.setType("integer");
                            } else {
                                pathParameter.setType(paramType);
                            }
                            // Note: 'format' to be added using annotations, hence skipped here.
                            operationAdaptor.getOperation().addParameter(pathParameter);
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the 'ParametersInfo' annotation and build swagger operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseParametersInfoAnnotationAttachment(ResourceInfo resource, Operation operation) {
        AnnAttachmentInfo parametersInfoAnnotationAttachment = resource.getAnnotationAttachmentInfo(
                SwaggerConstants.SWAGGER_PACKAGE_PATH, "ParametersInfo");
        if (null != parametersInfoAnnotationAttachment) {
            Map<String, AnnAttributeValue> parametersInfoAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                    (parametersInfoAnnotationAttachment);
            if (null != parametersInfoAnnAttributeValueMap.get("value")) {
                AnnAttributeValue[] parametersInfoValues = parametersInfoAnnAttributeValueMap.get("value")
                        .getAttributeValueArray();
                for (AnnAttributeValue parametersInfoValue : parametersInfoValues) {
                    AnnAttachmentInfo parameterInfoAnnotation = parametersInfoValue.getAnnotationAttachmentValue();
                    Map<String, AnnAttributeValue> parameterInfoAnnAttributeValueMap =
                            SwaggerUtils.convertToAttributeMap(parameterInfoAnnotation);
                    if (null != parameterInfoAnnAttributeValueMap.get("name")) {
                        if (null != operation.getParameters()) {
                            for (Parameter parameter : operation.getParameters()) {
                                if (parameter.getName().equals(parameterInfoAnnAttributeValueMap.get("name")
                                        .getStringValue())) {
                                    if (null != parameterInfoAnnAttributeValueMap.get("description")) {
                                        parameter.setDescription(parameterInfoAnnAttributeValueMap.get("description")
                                                .getStringValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the 'ResourceInfo' annotation and builds swagger operation.
     *
     * @param resource  The resource definition.
     * @param operation The swagger operation.
     */
    private void parseResourceInfoAnnotationAttachment(ResourceInfo resource, Operation operation) {
        AnnAttachmentInfo resourceConfigAnnotationAttachment = resource.getAnnotationAttachmentInfo(
                SwaggerConstants.SWAGGER_PACKAGE_PATH, "ResourceInfo");
        if (null != resourceConfigAnnotationAttachment) {
            Map<String, AnnAttributeValue> resourceConfigAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                    (resourceConfigAnnotationAttachment);
            this.createTagModel(resourceConfigAnnAttributeValueMap.get("tag"), operation);

            if (null != resourceConfigAnnAttributeValueMap.get("summary")) {
                operation.setSummary(resourceConfigAnnAttributeValueMap.get("summary").getStringValue());
            }
            if (null != resourceConfigAnnAttributeValueMap.get("description")) {
                operation.setDescription(resourceConfigAnnAttributeValueMap.get("description").getStringValue());
            }
            this.createExternalDocsModel(resourceConfigAnnAttributeValueMap.get("externalDoc"), operation);
        }
    }

    /**
     * Creates external docs swagger definitions.
     *
     * @param annotationAttributeValue The annotation attribute value for external docs.
     * @param operation                The swagger operation.
     */
    private void createExternalDocsModel(AnnAttributeValue annotationAttributeValue, Operation operation) {
        if (null != annotationAttributeValue) {
            AnnAttachmentInfo externalDocAnnotationAttachment = annotationAttributeValue.getAnnotationAttachmentValue();
            ExternalDocs externalDocs = new ExternalDocs();
            Map<String, AnnAttributeValue> externalDocsAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                    (externalDocAnnotationAttachment);
            if (null != externalDocsAnnAttributeValueMap.get("description")) {
                externalDocs.setDescription(externalDocsAnnAttributeValueMap.get("description").getStringValue());
            }
            if (null != externalDocsAnnAttributeValueMap.get("url")) {
                externalDocs.setUrl(externalDocsAnnAttributeValueMap.get("url").getStringValue());
            }

            operation.setExternalDocs(externalDocs);
        }
    }

    /**
     * Creates tag model for swagger operation.
     *
     * @param annotationAttributeValue The annotation attribute value which has tags.
     * @param operation                The swagger operation.
     */
    private void createTagModel(AnnAttributeValue annotationAttributeValue, Operation operation) {
        if (null != annotationAttributeValue) {
            if (annotationAttributeValue.getAttributeValueArray().length > 0) {
                List<String> tags = new LinkedList<>();
                for (AnnAttributeValue tagAttributeValue : annotationAttributeValue.getAttributeValueArray()) {
                    tags.add(tagAttributeValue.getStringValue());
                }
                operation.setTags(tags);
            }
        }
    }

    /**
     * Parses the produces annotation attachments and updates the swagger operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseProducesAnnotationAttachment(ResourceInfo resource, Operation operation) {
        AnnAttachmentInfo rConfigAnnAtchmnt = resource.getAnnotationAttachmentInfo(
                HttpConstants.HTTP_PACKAGE_PATH, HttpConstants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return;
        }
        AnnAttributeValue producesAttrVal = rConfigAnnAtchmnt
                .getAttributeValue(HttpConstants.ANN_RESOURCE_ATTR_PRODUCES);
        if (producesAttrVal == null) {
            return;
        }
        List<String> produces = getStringList(producesAttrVal.getAttributeValueArray());
        operation.setProduces(produces);
    }

    public static List<String> getStringList(AnnAttributeValue[] annAttributeValues) {
        List<String> produces = new LinkedList<>();
        for (int i = 0; i < annAttributeValues.length; i++) {
            produces.add(annAttributeValues[i].getStringValue());
        }
        return produces;
    }

    /**
     * Parses the consumes annotation attachments and updates the swagger operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseConsumesAnnotationAttachment(ResourceInfo resource, Operation operation) {
        AnnAttachmentInfo rConfigAnnAtchmnt = resource.getAnnotationAttachmentInfo(
                HttpConstants.HTTP_PACKAGE_PATH, HttpConstants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return;
        }
        AnnAttributeValue consumesAttrVal = rConfigAnnAtchmnt
                .getAttributeValue(HttpConstants.ANN_RESOURCE_ATTR_CONSUMES);
        if (consumesAttrVal == null) {
            return;
        }
        List<String> consumes = getStringList(consumesAttrVal.getAttributeValueArray());
        operation.setConsumes(consumes);
    }

    /**
     * Parses the 'ballerina/http@path' annotation and update the operation adaptor.
     *
     * @param resource         The ballerina resource definition.
     * @param operationAdaptor The operation adaptor.
     */
    private void parsePathAnnotationAttachment(ResourceInfo resource, OperationAdaptor operationAdaptor) {
        AnnAttachmentInfo rConfigAnnAtchmnt = resource.getAnnotationAttachmentInfo(
                HttpConstants.HTTP_PACKAGE_PATH, HttpConstants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnAtchmnt == null) {
            return;
        }
        AnnAttributeValue pathAttrVal = rConfigAnnAtchmnt
                .getAttributeValue(HttpConstants.ANN_RESOURCE_ATTR_PATH);

        if (null != pathAttrVal && pathAttrVal.getStringValue() != null) {
            operationAdaptor.setPath(pathAttrVal.getStringValue());
        }
    }

    /**
     * Parse the http method and update the operation adaptor. There can only be one http method annotation.
     *
     * @param resource         The ballerina resource definition.
     * @param operationAdaptor The operation adaptor which stored the http method.
     */
    private void parseHttpMethodAnnotationAttachment(ResourceInfo resource, OperationAdaptor operationAdaptor) {
        if (null != resource.getAnnotationAttachmentInfo(HttpConstants.HTTP_PACKAGE_PATH,
                                                         HttpConstants.HTTP_METHOD_GET)) {
            operationAdaptor.setHttpOperation(HttpConstants.HTTP_METHOD_GET);
        } else if (null != resource.getAnnotationAttachmentInfo(HttpConstants.HTTP_PACKAGE_PATH,
                                                                HttpConstants.HTTP_METHOD_POST)) {
            operationAdaptor.setHttpOperation(HttpConstants.HTTP_METHOD_POST);
        } else if (null != resource.getAnnotationAttachmentInfo(HttpConstants.HTTP_PACKAGE_PATH,
                                                                HttpConstants.HTTP_METHOD_PUT)) {
            operationAdaptor.setHttpOperation(HttpConstants.HTTP_METHOD_PUT);
        } else if (null != resource.getAnnotationAttachmentInfo(HttpConstants.HTTP_PACKAGE_PATH,
                                                                HttpConstants.HTTP_METHOD_DELETE)) {
            operationAdaptor.setHttpOperation(HttpConstants.HTTP_METHOD_DELETE);
        } else if (null != resource.getAnnotationAttachmentInfo(HttpConstants.HTTP_PACKAGE_PATH,
                                                                HttpConstants.HTTP_METHOD_HEAD)) {
            operationAdaptor.setHttpOperation(HttpConstants.HTTP_METHOD_HEAD);
        }
    }

    /**
     * Parse 'ResourceConfig' annotation attachment and build a resource operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseResourceConfigAnnotationAttachment(ResourceInfo resource, Operation operation) {
        AnnAttachmentInfo resourceConfigAnnotation = resource.getAnnotationAttachmentInfo(
                SwaggerConstants.SWAGGER_PACKAGE_PATH, "ResourceConfig");

        if (null != resourceConfigAnnotation) {
            Map<String, AnnAttributeValue> resourceConfigAnnAttributeValueMap = SwaggerUtils.convertToAttributeMap
                    (resourceConfigAnnotation);
            if (null != resourceConfigAnnAttributeValueMap.get("schemes")) {
                List<Scheme> schemes = new LinkedList<>();
                AnnAttributeValue[] schemesValues = resourceConfigAnnAttributeValueMap.get("schemes")
                        .getAttributeValueArray();
                for (AnnAttributeValue schemesValue : schemesValues) {
                    schemes.add(Scheme.forValue(schemesValue.getStringValue()));
                }
                operation.setSchemes(schemes);
            }
            // TODO: Implement security definitions.
            //this.createSecurityDefinitions(resourceConfigAnnotation.get().getAttributeNameValuePairs()
            // .get("authorizations"), operation);
        }
    }
}
