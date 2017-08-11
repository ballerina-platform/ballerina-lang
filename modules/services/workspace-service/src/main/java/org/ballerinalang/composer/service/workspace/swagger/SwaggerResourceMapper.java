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

package org.ballerinalang.composer.service.workspace.swagger;

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
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.services.dispatchers.http.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;

/**
 * This class will do resource mapping from ballerina to swagger.
 */
public class SwaggerResourceMapper {

    private static final String SWAGGER_PACKAGE_PATH = "ballerina.net.http.swagger";
    private static final String SWAGGER_PACKAGE = "swagger";
    private static final String HTTP_PACKAGE_PATH = "ballerina.net.http";
    private static final String HTTP_PACKAGE = "http";
    
    private final Swagger swaggerDefinition;
    
    SwaggerResourceMapper(Swagger swagger) {
        this.swaggerDefinition = swagger;
    }

    /**
     * This method will convert ballerina resource to swagger path objects.
     *
     * @param resources Resource array to be convert.
     * @return map of string and swagger path objects.
     */
    protected Map<String, Path> convertResourceToPath(Resource[] resources) {
        Map<String, Path> map = new HashMap<>();
        for (Resource subResource : resources) {
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
                case Constants.ANNOTATION_METHOD_GET:
                    path.get(operation);
                    break;
                case Constants.ANNOTATION_METHOD_PUT:
                    path.put(operation);
                    break;
                case Constants.ANNOTATION_METHOD_POST:
                    path.post(operation);
                    break;
                case Constants.ANNOTATION_METHOD_DELETE:
                    path.delete(operation);
                    break;
                case Constants.ANNOTATION_METHOD_OPTIONS:
                    path.options(operation);
                    break;
                case Constants.ANNOTATION_METHOD_PATCH:
                    path.patch(operation);
                    break;
                case "HEAD":
                    path.head(operation);
                    break;
                default:
                    break;
            }
        }
        return map;
    }


    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor
     *
     * @param resource Resource array to be convert.
     * @return @OperationAdaptor of string and swagger path objects.
     */
    private OperationAdaptor convertResourceToOperation(Resource resource) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            // Setting default values.
            op.setHttpOperation(Constants.HTTP_METHOD_GET);
            op.setPath('/' + resource.getName());
            Response response = new Response()
                    .description("Successful")
                    .example(MediaType.APPLICATION_JSON, "Ok");
            op.getOperation().response(200, response);
            op.getOperation().setOperationId(resource.getName());
            op.getOperation().setParameters(null);
    
            // Parsing annotations.
            this.parseHttpResourceConfig(resource, op);
            this.parseResourceConfigAnnotationAttachment(resource, op.getOperation());
            this.parseResourceInfoAnnotationAttachment(resource, op.getOperation());
            this.addResourceParameters(resource, op);
            this.parseParametersInfoAnnotationAttachment(resource, op.getOperation());
            this.parseResponsesAnnotationAttachment(resource, op.getOperation());
            
        }
        return op;

    }
    
    /**
     * Parses the 'http:resourceConfig' annotation attachment and build swagger operation.
     * @param resource The ballerina resource definition.
     * @param operationAdaptor The operation adaptor..
     */
    private void parseHttpResourceConfig(Resource resource, OperationAdaptor operationAdaptor) {
        Optional<AnnotationAttachment> responsesAnnotationAttachment = Arrays.stream(resource.getAnnotations())
                .filter(a -> this.checkIfHttpAnnotation(a) && "resourceConfig".equals(a.getName()))
                .findFirst();
        if (responsesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttributeValue> resourceConfigAnnotationAttributes =
                    responsesAnnotationAttachment.get().getAttributeNameValuePairs();
            if (null != resourceConfigAnnotationAttributes.get("methods") &&
                resourceConfigAnnotationAttributes.get("methods").getValueArray().length > 0) {
                AnnotationAttributeValue[] methodsValues =
                        resourceConfigAnnotationAttributes.get("methods").getValueArray();
                operationAdaptor.setHttpOperation(methodsValues[0].getLiteralValue().stringValue());
            }
            
            if (null != resourceConfigAnnotationAttributes.get("path")) {
                operationAdaptor.setPath((resourceConfigAnnotationAttributes.get("path")
                                                  .getLiteralValue().stringValue()));
            }
            
            if (null != resourceConfigAnnotationAttributes.get("produces") &&
                resourceConfigAnnotationAttributes.get("produces").getValueArray().length > 0) {
                List<String> produces = new LinkedList<>();
                AnnotationAttributeValue[] producesValues =
                        resourceConfigAnnotationAttributes.get("produces").getValueArray();
                for (AnnotationAttributeValue consumesValue : producesValues) {
                    produces.add(consumesValue.getLiteralValue().stringValue());
                }
                operationAdaptor.getOperation().setProduces(produces);
            }
            
            if (null != resourceConfigAnnotationAttributes.get("consumes") &&
                resourceConfigAnnotationAttributes.get("consumes").getValueArray().length > 0) {
                List<String> consumes = new LinkedList<>();
                AnnotationAttributeValue[] consumesValues =
                        resourceConfigAnnotationAttributes.get("consumes").getValueArray();
                for (AnnotationAttributeValue consumesValue : consumesValues) {
                    consumes.add(consumesValue.getLiteralValue().stringValue());
                }
                
                operationAdaptor.getOperation().setConsumes(consumes);
            }
        }
    }
    
    /**
     * Parses the 'Responses' annotation attachment and build swagger operation.
     * @param resource The ballerina resource definition.
     * @param op The swagger operation.
     */
    private void parseResponsesAnnotationAttachment(Resource resource, Operation op) {
        Optional<AnnotationAttachment> responsesAnnotationAttachment = Arrays.stream(resource.getAnnotations())
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "Responses".equals(a.getName()))
                .findFirst();
        if (responsesAnnotationAttachment.isPresent()) {
            if (null != responsesAnnotationAttachment.get().getAttributeNameValuePairs().get("value")) {
                AnnotationAttributeValue[] responsesValues = responsesAnnotationAttachment.get()
                        .getAttributeNameValuePairs().get("value").getValueArray();
                if (responsesValues.length > 0) {
                    Map<String, Response> responses = new HashMap<>();
                    for (AnnotationAttributeValue responsesValue : responsesValues) {
                        AnnotationAttachment responseAnnotationAttachment = responsesValue.getAnnotationValue();
                        if (null != responseAnnotationAttachment.getAttributeNameValuePairs().get("code")) {
                            String code = responseAnnotationAttachment.getAttributeNameValuePairs()
                                    .get("code").getLiteralValue().stringValue();
                            Response response = new Response();
                            if (null != responseAnnotationAttachment.getAttributeNameValuePairs().get("description")) {
                                response.setDescription(responseAnnotationAttachment.getAttributeNameValuePairs()
                                        .get("description").getLiteralValue().stringValue());
                            }
                            // TODO: Parse 'response' attribute for $.paths./resource-path.responses[*]["code"].schema
                            this.createHeadersModel(responseAnnotationAttachment.getAttributeNameValuePairs()
                                    .get("headers"), response);
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
     * @param annotationAttributeValue The annotation attribute value which has the headers.
     * @param response The swagger response.
     */
    private void createHeadersModel(AnnotationAttributeValue annotationAttributeValue, Response response) {
        if (null != annotationAttributeValue) {
            AnnotationAttributeValue[] headersValueArray = annotationAttributeValue.getValueArray();
            for (AnnotationAttributeValue headersValue : headersValueArray) {
                AnnotationAttachment headerAnnotationAttachment = headersValue.getAnnotationValue();
                Map<String, Property> headers = new HashMap<>();
                if (null != headerAnnotationAttachment.getAttributeNameValuePairs().get("name") &&
                    null != headerAnnotationAttachment.getAttributeNameValuePairs().get("headerType")) {
                    String headerName = headerAnnotationAttachment.getAttributeNameValuePairs().get("name")
                            .getLiteralValue().stringValue();
                    String type = headerAnnotationAttachment.getAttributeNameValuePairs().get("headerType")
                            .getLiteralValue().stringValue();
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
                        if (null != headerAnnotationAttachment.getAttributeNameValuePairs().get("description")) {
                            property.setDescription(headerAnnotationAttachment.getAttributeNameValuePairs()
                                    .get("description").getLiteralValue().stringValue());
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
     * @param resource The ballerina resource definition.
     * @param operationAdaptor The swagger operation.
     */
    private void addResourceParameters(Resource resource, OperationAdaptor operationAdaptor) {
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
            messageParameter.setName(resource.getParameterDefs()[0].getName());
            RefModel refModel = new RefModel();
            refModel.setReference("Message");
            messageParameter.setSchema(refModel);
            operationAdaptor.getOperation().addParameter(messageParameter);
        }
        
        for (ParameterDef parameterDef : resource.getParameterDefs()) {
            String typeName = parameterDef.getTypeName().getName();
            if (!typeName.equalsIgnoreCase("message") && parameterDef.getAnnotations() != null) {
                AnnotationAttachment parameterAnnotation = parameterDef.getAnnotations()[0];
                // Add query parameter
                if (this.checkIfHttpAnnotation(parameterAnnotation) &&
                                                        parameterAnnotation.getName().equalsIgnoreCase("QueryParam")) {
                    QueryParameter queryParameter = new QueryParameter();
                    // Set in value.
                    queryParameter.setIn("query");
                    // Set parameter name
                    String parameterName = parameterDef.getAnnotations()[0].getValue();
                    if ((parameterName == null) || parameterName.isEmpty()) {
                        parameterName = parameterDef.getName();
                    }
                    queryParameter.setName(parameterName);
                    // Note: 'description' to be added using annotations, hence skipped here.
                    // Setting false to required(as per swagger spec). This can be overridden while parsing annotations.
                    queryParameter.required(false);
                    // Note: 'allowEmptyValue' to be added using annotations, hence skipped here.
                    // Set type
                    if ("int".equals(typeName)) {
                        queryParameter.setType("integer");
                    } else {
                        queryParameter.setType(typeName);
                    }
                    // Note: 'format' to be added using annotations, hence skipped here.
                    operationAdaptor.getOperation().addParameter(queryParameter);
                }
                if (this.checkIfHttpAnnotation(parameterAnnotation) &&
                                                        parameterAnnotation.getName().equalsIgnoreCase("PathParam")) {
                    PathParameter pathParameter = new PathParameter();
                    // Set in value
                    pathParameter.setIn("path");
                    // Set parameter name
                    String parameterName = parameterDef.getAnnotations()[0].getValue();
                    if ((parameterName == null) || parameterName.isEmpty()) {
                        parameterName = parameterDef.getName();
                    }
                    pathParameter.setName(parameterName);
                    // Note: 'description' to be added using annotations, hence skipped here.
                    // Note: 'allowEmptyValue' to be added using annotations, hence skipped here.
                    // Set type
                    if ("int".equals(typeName)) {
                        pathParameter.setType("integer");
                    } else {
                        pathParameter.setType(typeName);
                    }
                    // Note: 'format' to be added using annotations, hence skipped here.
                    operationAdaptor.getOperation().addParameter(pathParameter);
                }
            }
        }
    }
    
    /**
     * Parses the 'ParametersInfo' annotation and build swagger operation.
     * @param resource The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseParametersInfoAnnotationAttachment(Resource resource, Operation operation) {
        Optional<AnnotationAttachment> parametersInfoAnnotationAttachment = Arrays.stream(resource.getAnnotations())
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ParametersInfo".equals(a.getName()))
                .findFirst();
        if (parametersInfoAnnotationAttachment.isPresent()) {
            if (null != parametersInfoAnnotationAttachment.get().getAttributeNameValuePairs().get("value")) {
                AnnotationAttributeValue[] parametersInfoValues = parametersInfoAnnotationAttachment.get()
                        .getAttributeNameValuePairs().get("value").getValueArray();
                for (AnnotationAttributeValue parametersInfoValue : parametersInfoValues) {
                    AnnotationAttachment parameterInfoAnnotation = parametersInfoValue.getAnnotationValue();
                    if (null != parameterInfoAnnotation.getAttributeNameValuePairs().get("name")) {
                        if (null != operation.getParameters()) {
                            for (Parameter parameter : operation.getParameters()) {
                                if (parameter.getName().equals(parameterInfoAnnotation.getAttributeNameValuePairs()
                                        .get("name").getLiteralValue().stringValue())) {
                                    if (null != parameterInfoAnnotation.getAttributeNameValuePairs()
                                            .get("description")) {
                                        parameter.setDescription(parameterInfoAnnotation.getAttributeNameValuePairs()
                                                .get("description").getLiteralValue().stringValue());
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
     * @param resource The resource definition.
     * @param operation The swagger operation.
     */
    private void parseResourceInfoAnnotationAttachment(Resource resource, Operation operation) {
        Optional<AnnotationAttachment> resourceConfigAnnotationAttachment = Arrays.stream(resource.getAnnotations())
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ResourceInfo".equals(a.getName()))
                .findFirst();
        if (resourceConfigAnnotationAttachment.isPresent()) {
            this.createTagModel(resourceConfigAnnotationAttachment.get().getAttributeNameValuePairs()
                    .get("tag"), operation);
            
            if (null != resourceConfigAnnotationAttachment.get().getAttributeNameValuePairs().get("summary")) {
                operation.setSummary(resourceConfigAnnotationAttachment.get().getAttributeNameValuePairs()
                        .get("summary").getLiteralValue().stringValue());
            }
            if (null != resourceConfigAnnotationAttachment.get().getAttributeNameValuePairs().get("description")) {
                operation.setDescription(resourceConfigAnnotationAttachment.get().getAttributeNameValuePairs()
                        .get("description").getLiteralValue().stringValue());
            }
            this.createExternalDocsModel(resourceConfigAnnotationAttachment.get().getAttributeNameValuePairs()
                    .get("externalDoc"), operation);
        }
    }
    
    /**
     * Creates external docs swagger definitions.
     * @param annotationAttributeValue The annotation attribute value for external docs.
     * @param operation The swagger operation.
     */
    private void createExternalDocsModel(AnnotationAttributeValue annotationAttributeValue, Operation operation) {
        if (null != annotationAttributeValue) {
            AnnotationAttachment externalDocAnnotationAttachment = annotationAttributeValue.getAnnotationValue();
            ExternalDocs externalDocs = new ExternalDocs();
            if (null != externalDocAnnotationAttachment.getAttributeNameValuePairs().get("description")) {
                externalDocs.setDescription(externalDocAnnotationAttachment.getAttributeNameValuePairs()
                        .get("description").getLiteralValue().stringValue());
            }
            if (null != externalDocAnnotationAttachment.getAttributeNameValuePairs().get("url")) {
                externalDocs.setUrl(externalDocAnnotationAttachment.getAttributeNameValuePairs().get("url")
                        .getLiteralValue().stringValue());
            }
    
            operation.setExternalDocs(externalDocs);
        }
    }
    
    /**
     * Creates tag model for swagger operation.
     * @param annotationAttributeValue The annotation attribute value which has tags.
     * @param operation The swagger operation.
     */
    private void createTagModel(AnnotationAttributeValue annotationAttributeValue, Operation operation) {
        if (null != annotationAttributeValue) {
            if (annotationAttributeValue.getValueArray().length > 0) {
                List<String> tags = new LinkedList<>();
                for (AnnotationAttributeValue tagAttributeValue : annotationAttributeValue.getValueArray()) {
                    tags.add(tagAttributeValue.getLiteralValue().stringValue());
                }
                operation.setTags(tags);
            }
        }
    }
    
    /**
     * Parse 'ResourceConfig' annotation attachment and build a resource operation.
     * @param resource The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseResourceConfigAnnotationAttachment(Resource resource, Operation operation) {
        Optional<AnnotationAttachment> resourceConfigAnnotation = Arrays.stream(resource.getAnnotations())
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ResourceConfig".equals(a.getName()))
                .findFirst();
    
        if (resourceConfigAnnotation.isPresent()) {
            if (null != resourceConfigAnnotation.get().getAttributeNameValuePairs().get("schemes")) {
                List<Scheme> schemes = new LinkedList<>();
                AnnotationAttributeValue[] schemesValues = resourceConfigAnnotation.get().getAttributeNameValuePairs()
                        .get("schemes").getValueArray();
                for (AnnotationAttributeValue schemesValue : schemesValues) {
                    schemes.add(Scheme.forValue(schemesValue.getLiteralValue().stringValue()));
                }
                operation.setSchemes(schemes);
            }
            // TODO: Implement security definitions.
            //this.createSecurityDefinitions(resourceConfigAnnotation.get().getAttributeNameValuePairs()
            // .get("authorizations"), operation);
        }
    }
    
    /**
     * Checks if an annotation belongs to ballerina.net.http.swagger package.
     * @param annotationAttachment The annotation.
     * @return true if belongs to ballerina.net.http.swagger package, else false.
     */
    private boolean checkIfSwaggerAnnotation(AnnotationAttachment annotationAttachment) {
        return SWAGGER_PACKAGE_PATH.equals(annotationAttachment.getPkgPath()) &&
               SWAGGER_PACKAGE.equals(annotationAttachment.getPkgName());
    }
    
    /**
     * Checks if an annotation belongs to ballerina.net.http package.
     * @param annotationAttachment The annotation.
     * @return true if belongs to ballerina.net.http package, else false.
     */
    private boolean checkIfHttpAnnotation(AnnotationAttachment annotationAttachment) {
        return HTTP_PACKAGE_PATH.equals(annotationAttachment.getPkgPath()) &&
               HTTP_PACKAGE.equals(annotationAttachment.getPkgName());
    }
}
