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
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.services.dispatchers.http.Constants;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;

/**
 * This class will do resource mapping from ballerina to swagger.
 */
public class SwaggerResourceMapper {

    private static final String SWAGGER_PACKAGE_PATH = "ballerina.net.http.swagger";
    private static final String SWAGGER_PACKAGE = "swagger";
    private static final String HTTP_PACKAGE_PATH = "ballerina.net.http";
    private static final String HTTP_PACKAGE = "http";
    private static final String X_MULTI_OPERATIONS = "x-MULTI";
    private static final String X_HTTP_METHODS = "x-METHODS";
    
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
    protected Map<String, Path> convertResourceToPath(List<? extends ResourceNode> resources) {
        Map<String, Path> pathMap = new HashMap<>();
        for (ResourceNode resource : resources) {
            if (this.getHttpMethods(resource).size() > 1) {
                useMultiResourceMapper(pathMap, resource);
            } else {
                useDefaultResourceMapper(pathMap, resource);
            }
        }
        return pathMap;
    }
    
    /**
     * Resource mapper when a resource has more than 1 http method.
     * @param pathMap The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useMultiResourceMapper(Map<String, Path> pathMap, ResourceNode resource) {
        Set<String> httpMethods = this.getHttpMethods(resource);
        String path = this.getPath(resource);
        Operation operation = null;
        boolean hasGetOnly = true;
        for (String httpMethod : httpMethods) {
            if (!httpMethod.equalsIgnoreCase("get")) {
                hasGetOnly = false;
                break;
            }
        }
        
        if (hasGetOnly) {
            operation = this.convertResourceToOperation(resource, null).getOperation();
        } else {
            for (String httpMethod : httpMethods) {
                if (!httpMethod.equalsIgnoreCase("get")) {
                    operation = this.convertResourceToOperation(resource,
                            httpMethod.toLowerCase(Locale.getDefault())).getOperation();
                    break;
                }
            }
        }
    
        if (operation != null) {
            operation.setVendorExtension(X_HTTP_METHODS, httpMethods);
        }
    
        Path pathObject = new Path();
        pathObject.setVendorExtension(X_MULTI_OPERATIONS, operation);
        pathMap.put(path, pathObject);
    }
    
    /**
     * Resource mapper when a resource has only one http method.
     * @param pathMap The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useDefaultResourceMapper(Map<String, Path> pathMap, ResourceNode resource) {
        OperationAdaptor operationAdaptor = this.convertResourceToOperation(resource, null);
        Path path = pathMap.get(operationAdaptor.getPath());
        //TODO this check need to be improve to avoid repetition checks and http head support need to add.
        if (path == null) {
            path = new Path();
            pathMap.put(operationAdaptor.getPath(), path);
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
    
    
    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor
     *
     * @param resource Resource array to be convert.
     * @return @OperationAdaptor of string and swagger path objects.
     */
    private OperationAdaptor convertResourceToOperation(ResourceNode resource, String httpMethod) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            // Setting default values.
            op.setHttpOperation(Constants.HTTP_METHOD_GET);
            op.setPath('/' + resource.getName().getValue());
            Response response = new Response()
                    .description("Successful")
                    .example(MediaType.APPLICATION_JSON, "Ok");
            op.getOperation().response(200, response);
            op.getOperation().setOperationId(resource.getName().getValue());
            op.getOperation().setParameters(null);
    
            // Parsing annotations.
            this.parseHttpResourceConfig(resource, op);
            if (null != httpMethod) {
                op.setHttpOperation(httpMethod);
            }
            
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
    private void parseHttpResourceConfig(ResourceNode resource, OperationAdaptor operationAdaptor) {
        Optional<? extends AnnotationAttachmentNode> responsesAnnotationAttachment = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfHttpAnnotation(a) && "resourceConfig".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (responsesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> configAttributes =
                    this.listToMap(responsesAnnotationAttachment.get());
            if (configAttributes.containsKey("methods") && configAttributes.get("methods").getValueArray().size() > 0) {
                List<AnnotationAttachmentAttributeValueNode> methodsValues =
                        configAttributes.get("methods").getValueArray();
                // Since there is only one http method.
                operationAdaptor.setHttpOperation(this.getStringLiteralValue(methodsValues.get(0)));
            }
            
            if (configAttributes.containsKey("path")) {
                operationAdaptor.setPath(this.getStringLiteralValue(configAttributes.get("path")));
            }
            
            if (configAttributes.containsKey("produces") && configAttributes.get("produces").getValueArray().size() > 0) {
                List<String> produces = new LinkedList<>();
                List<AnnotationAttachmentAttributeValueNode> producesValues =
                        configAttributes.get("produces").getValueArray();
                for (AnnotationAttachmentAttributeValueNode producesValue : producesValues) {
                    produces.add(this.getStringLiteralValue(producesValue));
                }
                operationAdaptor.getOperation().setProduces(produces);
            }
            
            if (configAttributes.containsKey("consumes") && configAttributes.get("consumes").getValueArray().size() > 0) {
                List<String> consumes = new LinkedList<>();
                List<AnnotationAttachmentAttributeValueNode> consumesValues =
                        configAttributes.get("consumes").getValueArray();
                for (AnnotationAttachmentAttributeValueNode consumesValue : consumesValues) {
                    consumes.add(this.getStringLiteralValue(consumesValue));
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
    private void parseResponsesAnnotationAttachment(ResourceNode resource, Operation op) {
        Optional<? extends AnnotationAttachmentNode> responsesAnnotationAttachment = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "Responses".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (responsesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> responsesAttributes =
                    this.listToMap(responsesAnnotationAttachment.get());
            if (responsesAttributes.containsKey("value")) {
                List<AnnotationAttachmentAttributeValueNode> responsesValues = responsesAttributes.get("value").getValueArray();
                if (responsesValues.size() > 0) {
                    Map<String, Response> responses = new HashMap<>();
                    for (AnnotationAttachmentAttributeValueNode responsesValue : responsesValues) {
                        AnnotationAttachmentNode responseAnnotationAttachment = (AnnotationAttachmentNode)responsesValue;
                        
                        Map<String, AnnotationAttachmentAttributeValueNode>
                                responseAttributes = this.listToMap
                                (responseAnnotationAttachment);
                        if (responseAttributes.containsKey("code")) {
                            String code = this.getStringLiteralValue(responseAttributes.get("code"));
                            Response response = new Response();
                            if (responseAttributes.containsKey("description")) {
                                response.setDescription(this.getStringLiteralValue(responseAttributes.get("description")));
                            }
                            // TODO: Parse 'response' attribute for $.paths./resource-path.responses[*]["code"].schema
                            this.createHeadersModel(responseAttributes.get("headers"), response);
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
    private void createHeadersModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Response response) {
        if (null != annotationAttributeValue) {
            List<AnnotationAttachmentAttributeValueNode> headersValueArray = annotationAttributeValue.getValueArray();
            for (AnnotationAttachmentAttributeValueNode headersValue : headersValueArray) {
                AnnotationAttachmentNode headerAnnotationAttachment = (AnnotationAttachmentNode)headersValue;
                Map<String, Property> headers = new HashMap<>();
                Map<String, AnnotationAttachmentAttributeValueNode> headersAttributes =
                        this.listToMap(headerAnnotationAttachment);
                if (headersAttributes.containsKey("name") && headersAttributes.containsKey("headerType")) {
                    String headerName = this.getStringLiteralValue(headersAttributes.get("name"));
                    String type = this.getStringLiteralValue(headersAttributes.get("headerType"));
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
                        if (headersAttributes.containsKey("description")) {
                            property.setDescription(this.getStringLiteralValue(headersAttributes.get("description")));
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
    private void addResourceParameters(ResourceNode resource, OperationAdaptor operationAdaptor) {
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
            messageParameter.setName(resource.getParameters().get(0).getName().getValue());
            RefModel refModel = new RefModel();
            refModel.setReference("Message");
            messageParameter.setSchema(refModel);
            operationAdaptor.getOperation().addParameter(messageParameter);
        }
        
        for (VariableNode parameterDef : resource.getParameters()) {
            String typeName = parameterDef.getTypeNode().toString();
            if (!typeName.equalsIgnoreCase("message") && parameterDef.getAnnotationAttachments() != null) {
                AnnotationAttachmentNode parameterAnnotation = parameterDef.getAnnotationAttachments().get(0);
                // Add query parameter
                if (this.checkIfHttpAnnotation(parameterAnnotation) &&
                                parameterAnnotation.getAnnotationName().getValue().equalsIgnoreCase("QueryParam")) {
                    QueryParameter queryParameter = new QueryParameter();
                    // Set in value.
                    queryParameter.setIn("query");
                    // Set parameter name
                    String parameterName = parameterDef.getAnnotationAttachments().get(0).getAnnotationName().getValue();
                    if ((parameterName == null) || parameterName.isEmpty()) {
                        parameterName = parameterDef.getName().getValue();
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
                                    parameterAnnotation.getAnnotationName().getValue().equalsIgnoreCase("PathParam")) {
                    PathParameter pathParameter = new PathParameter();
                    // Set in value
                    pathParameter.setIn("path");
                    // Set parameter name
                    String parameterName = parameterDef.getAnnotationAttachments().get(0).getAnnotationName().getValue();
                    if ((parameterName == null) || parameterName.isEmpty()) {
                        parameterName = parameterDef.getName().getValue();
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
    private void parseParametersInfoAnnotationAttachment(ResourceNode resource, Operation operation) {
        Optional<? extends AnnotationAttachmentNode> parametersInfoAnnotationAttachment = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ParametersInfo".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (parametersInfoAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> infoAttributes =
                    this.listToMap(parametersInfoAnnotationAttachment.get());
            if (infoAttributes.containsKey("value")) {
                List<AnnotationAttachmentAttributeValueNode> parametersInfoValues = infoAttributes.get("value").getValueArray();
                for (AnnotationAttachmentAttributeValueNode parametersInfoValue : parametersInfoValues) {
                    AnnotationAttachmentNode parameterInfoAnnotation = (AnnotationAttachmentNode)parametersInfoValue;
                    Map<String, AnnotationAttachmentAttributeValueNode>
                            parameterAttributes = this.listToMap(parameterInfoAnnotation);
                    if (parameterAttributes.containsKey("name")) {
                        if (null != operation.getParameters()) {
                            for (Parameter parameter : operation.getParameters()) {
                                if (parameter.getName().equals(this.getStringLiteralValue(parameterAttributes.get("name")))) {
                                    if (parameterAttributes.containsKey("description")) {
                                        parameter.setDescription(this.getStringLiteralValue(parameterAttributes.get("description")));
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
    private void parseResourceInfoAnnotationAttachment(ResourceNode resource, Operation operation) {
        Optional<? extends AnnotationAttachmentNode> resourceConfigAnnotationAttachment = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ResourceInfo".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (resourceConfigAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> infoAttributes =
                    this.listToMap(resourceConfigAnnotationAttachment.get());
            this.createTagModel(infoAttributes.get("tag"), operation);
            
            if (infoAttributes.containsKey("summary")) {
                operation.setSummary(this.getStringLiteralValue(infoAttributes.get("summary")));
            }
            if (infoAttributes.containsKey("description")) {
                operation.setDescription(this.getStringLiteralValue(infoAttributes.get("description")));
            }
            this.createExternalDocsModel(infoAttributes.get("externalDoc"), operation);
        }
    }
    
    /**
     * Creates external docs swagger definitions.
     * @param annotationAttributeValue The annotation attribute value for external docs.
     * @param operation The swagger operation.
     */
    private void createExternalDocsModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Operation operation) {
        if (null != annotationAttributeValue) {
            AnnotationAttachmentNode externalDocAnnotationAttachment = (AnnotationAttachmentNode)annotationAttributeValue;
            ExternalDocs externalDocs = new ExternalDocs();
    
            Map<String, AnnotationAttachmentAttributeValueNode> externalDocAttributes =
                    this.listToMap(externalDocAnnotationAttachment);
            if (externalDocAttributes.containsKey("description")) {
                externalDocs.setDescription(this.getStringLiteralValue(externalDocAttributes.get("description")));
            }
            if (externalDocAttributes.containsKey("url")) {
                externalDocs.setUrl(this.getStringLiteralValue(externalDocAttributes.get("url")));
            }
    
            operation.setExternalDocs(externalDocs);
        }
    }
    
    /**
     * Creates tag model for swagger operation.
     * @param annotationAttributeValue The annotation attribute value which has tags.
     * @param operation The swagger operation.
     */
    private void createTagModel(AnnotationAttachmentAttributeValueNode annotationAttributeValue, Operation operation) {
        if (null != annotationAttributeValue) {
            if (annotationAttributeValue.getValueArray().size() > 0) {
                List<String> tags = new LinkedList<>();
                for (AnnotationAttachmentAttributeValueNode tagAttributeValue : annotationAttributeValue.getValueArray()) {
                    tags.add(this.getStringLiteralValue(tagAttributeValue));
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
    private void parseResourceConfigAnnotationAttachment(ResourceNode resource, Operation operation) {
        Optional<? extends AnnotationAttachmentNode> resourceConfigAnnotation = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfSwaggerAnnotation(a) && "ResourceConfig".equals(a.getAnnotationName().getValue()))
                .findFirst();
    
        if (resourceConfigAnnotation.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> configAttributes =
                    this.listToMap(resourceConfigAnnotation.get());
            if (configAttributes.containsKey("schemes")) {
                List<Scheme> schemes = new LinkedList<>();
                for (AnnotationAttachmentAttributeValueNode schemesValue : configAttributes.get("schemes").getValueArray()) {
                    if (null != Scheme.forValue(this.getStringLiteralValue(schemesValue))) {
                        schemes.add(Scheme.forValue(this.getStringLiteralValue(schemesValue)));
                    }
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
    private boolean checkIfSwaggerAnnotation(AnnotationAttachmentNode annotationAttachment) {
        return true;
//        return SWAGGER_PACKAGE_PATH.equals(annotationAttachment.getPkgPath()) &&
//               SWAGGER_PACKAGE.equals(annotationAttachment.getPkgName());
    }
    
    /**
     * Checks if an annotation belongs to ballerina.net.http package.
     * @param annotationAttachment The annotation.
     * @return true if belongs to ballerina.net.http package, else false.
     */
    private boolean checkIfHttpAnnotation(AnnotationAttachmentNode annotationAttachment) {
        return true;
//        return HTTP_PACKAGE_PATH.equals(annotationAttachment.getPkgPath()) &&
//               HTTP_PACKAGE.equals(annotationAttachment.getPkgName());
    }
    
    /**
     * Gets the http methods of a resource.
     * @param resource The ballerina resource.
     * @return A list of http methods.
     */
    private Set<String> getHttpMethods(ResourceNode resource) {
        Optional<? extends AnnotationAttachmentNode> responsesAnnotationAttachment = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfHttpAnnotation(a) && "resourceConfig".equals(a.getAnnotationName().getValue()))
                .findFirst();
        Set<String> httpMethods = new LinkedHashSet<>();
        if (responsesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> responsesAttributes =
                    this.listToMap(responsesAnnotationAttachment.get());
            if (responsesAttributes.containsKey("methods") && responsesAttributes.get("methods").getValueArray().size() > 0) {
                for (AnnotationAttachmentAttributeValueNode methodsValue : responsesAttributes.get("methods").getValueArray()) {
                    httpMethods.add(this.getStringLiteralValue(methodsValue));
                }
            }
        }
        
        if (httpMethods.isEmpty()) {
            // By default get is supported.
            httpMethods.add("GET");
        }
        return httpMethods;
    }
    
    /**
     * Gets the path value of the @http:resourceConfig.
     * @param resource The ballerina resource.
     * @return The path value.
     */
    private String getPath(ResourceNode resource) {
        String path = "/" + resource.getName();
        Optional<? extends AnnotationAttachmentNode> responsesAnnotationAttachment = resource.getAnnotationAttachments().stream()
                .filter(a -> this.checkIfHttpAnnotation(a) && "resourceConfig".equals(a.getAnnotationName().getValue()))
                .findFirst();
        if (responsesAnnotationAttachment.isPresent()) {
            Map<String, AnnotationAttachmentAttributeValueNode> configAttributes =
                    this.listToMap(responsesAnnotationAttachment.get());
            if (configAttributes.containsKey("path")) {
                path = this.getStringLiteralValue(configAttributes.get("path"));
            }
        }
        
        return path;
    }
    
    private Map<String, AnnotationAttachmentAttributeValueNode> listToMap(AnnotationAttachmentNode annotation) {
        return annotation.geAttributes().stream().collect(
                Collectors.toMap(AnnotationAttachmentAttributeNode::getName, AnnotationAttachmentAttributeNode
                        ::getValue));
    }
    
    private String getStringLiteralValue(AnnotationAttachmentAttributeValueNode valueNode) {
        return ((LiteralNode)valueNode.getValue()).getValue().toString();
    }
}
