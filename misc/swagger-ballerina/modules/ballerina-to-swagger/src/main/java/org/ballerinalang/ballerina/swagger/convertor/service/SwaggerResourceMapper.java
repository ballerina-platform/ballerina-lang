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

package org.ballerinalang.ballerina.swagger.convertor.service;

import com.google.common.collect.Lists;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import org.ballerinalang.ballerina.swagger.convertor.ConverterUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;

import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;

/**
 * This class will do resource mapping from ballerina to swagger.
 */
public class SwaggerResourceMapper {

    private static final String X_MULTI_OPERATIONS = "x-MULTI";
    private static final String X_HTTP_METHODS = "x-METHODS";
    private final String httpAlias;
    private final String swaggerAlias;
    private final Swagger swaggerDefinition;

    /**
     * Initializes a resource parser for swagger.
     *
     * @param swagger      The swagger definition.
     * @param httpAlias    The alias for ballerina/http package.
     * @param swaggerAlias The alias for ballerina.swagger package.
     */
    SwaggerResourceMapper(Swagger swagger, String httpAlias, String swaggerAlias) {
        this.httpAlias = httpAlias;
        this.swaggerAlias = swaggerAlias;
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
            if (this.getHttpMethods(resource, false).size() == 0 ||
                    this.getHttpMethods(resource, false).size() > 1) {
                useMultiResourceMapper(pathMap, resource);
            } else {
                useDefaultResourceMapper(pathMap, resource);
            }
        }
        return pathMap;
    }

    /**
     * Resource mapper when a resource has more than 1 http method.
     *
     * @param pathMap  The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useMultiResourceMapper(Map<String, Path> pathMap, ResourceNode resource) {
        List<String> httpMethods = this.getHttpMethods(resource, true);
        String path = this.getPath(resource);
        Path pathObject = new Path();
        Operation operation = null;
        //Iterate through http methods and fill path map.
        operation = this.convertResourceToOperation(resource, null).getOperation();
        if (operation != null) {
            operation.setVendorExtension(X_HTTP_METHODS, httpMethods);
        }
        pathObject.setVendorExtension(X_MULTI_OPERATIONS, operation);
        pathMap.put(path, pathObject);
    }

    /**
     * Resource mapper when a resource has only one http method.
     *
     * @param pathMap  The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useDefaultResourceMapper(Map<String, Path> pathMap, ResourceNode resource) {
        OperationAdaptor operationAdaptor = this.convertResourceToOperation(resource, null);
        String httpOperation = getHttpMethods(resource , true).get(0);
        operationAdaptor.setHttpOperation(httpOperation);
        Path path = pathMap.get(operationAdaptor.getPath());
        if (path == null) {
            path = new Path();
            pathMap.put(operationAdaptor.getPath(), path);
        }
        //String httpOperation = operationAdaptor.getHttpOperation();
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

    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor.
     *
     * @param resource Resource array to be convert.
     * @return Operation Adaptor object of given resource
     */
    private OperationAdaptor convertResourceToOperation(ResourceNode resource, String httpMethod) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            // Setting default values.
            op.setHttpOperation(HttpConstants.HTTP_METHOD_GET);
            op.setPath('/' + resource.getName().getValue());
            Response response = new Response()
                    .description("Successful")
                    .example(MediaType.APPLICATION_JSON, "Ok");
            op.getOperation().response(200, response);

            // Replacing all '_' with ' ' to keep the consistency with what we are doing in swagger -> bal
            // @see BallerinaOperation#buildContext
            String resName = resource.getName().getValue().replaceAll("_", " ");
            op.getOperation().setOperationId(resName);
            op.getOperation().setParameters(null);
            // Parsing annotations.
            this.parseResourceConfigAnnotationAttachment(resource, op);
            if (null != httpMethod) {
                op.setHttpOperation(httpMethod);
            }

            this.parseResourceInfoAnnotationAttachment(resource, op.getOperation());
            this.addResourceParameters(resource, op);
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
    private void parseResponsesAnnotationAttachment(ResourceNode resource, Operation op) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("Responses", swaggerAlias,
                resource.getAnnotationAttachments());

        if (annotation != null) {

            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);

            if (attributes.containsKey("value")) {
                BLangArrayLiteral valueArray = (BLangArrayLiteral) attributes.get("value");

                if (valueArray.getExpressions().size() > 0) {
                    Map<String, Response> responses = new HashMap<>();

                    for (ExpressionNode expr : valueArray.getExpressions()) {
                        List<BLangRecordKeyValue> resList = ((BLangRecordLiteral) expr).getKeyValuePairs();
                        Map<String, BLangExpression> resAttributes = ConverterUtils.listToMap(resList);

                        if (resAttributes.containsKey("code")) {
                            String code = ConverterUtils.getStringLiteralValue(resAttributes.get("code"));
                            Response response = new Response();
                            if (resAttributes.containsKey("description")) {
                                response.setDescription(
                                        ConverterUtils.getStringLiteralValue(resAttributes.get("description")));
                            }
                            // TODO: Parse 'response' attribute for $.paths./resource-path.responses[*]["code"].schema
                            this.createHeadersModel(resAttributes.get("headers"), response);
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
     * @param annotationExpression The annotation attribute value which has the headers.
     * @param response             The swagger response.
     */
    private void createHeadersModel(BLangExpression annotationExpression, Response response) {
        if (null != annotationExpression) {
            BLangArrayLiteral headerArray = (BLangArrayLiteral) annotationExpression;

            for (ExpressionNode headersValue : headerArray.getExpressions()) {
                List<BLangRecordKeyValue> headerList = ((BLangRecordLiteral) headersValue).getKeyValuePairs();
                Map<String, BLangExpression> headersAttributes = ConverterUtils.listToMap(headerList);
                Map<String, Property> headers = new HashMap<>();

                if (headersAttributes.containsKey("name") && headersAttributes.containsKey("headerType")) {
                    String headerName = ConverterUtils.getStringLiteralValue(headersAttributes.get("name"));
                    String type = ConverterUtils.getStringLiteralValue(headersAttributes.get("headerType"));
                    Property property = getSwaggerProperty(type);

                    if (headersAttributes.containsKey("description")) {
                        property.setDescription(
                                ConverterUtils.getStringLiteralValue(headersAttributes.get("description")));
                    }
                    headers.put(headerName, property);
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
    private void addResourceParameters(ResourceNode resource, OperationAdaptor operationAdaptor) {
        //Set Path
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ResourceConfig", httpAlias,
                resource.getAnnotationAttachments());


        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
            Map<String, BLangExpression> recordsMap = ConverterUtils.listToMap(list);

            if (recordsMap.containsKey("path") && recordsMap.get("path") != null) {
                String path =  recordsMap.get("path").toString().trim();
                operationAdaptor.setPath(path);
            } else {
                operationAdaptor.setPath("/");
            }
        }

        if (!"get".equalsIgnoreCase(operationAdaptor.getHttpOperation())) {
        
            // Creating request body - required.
            ModelImpl messageModel = new ModelImpl();
            messageModel.setType("object");
            Map<String, Model> definitions = new HashMap<>();
            if (!definitions.containsKey("Request")) {
                definitions.put("Request", messageModel);
                this.swaggerDefinition.setDefinitions(definitions);
            }
        
            // Creating "Request rq" parameter
            BodyParameter messageParameter = new BodyParameter();
            messageParameter.setName(resource.getParameters().get(0).getName().getValue());
            RefModel refModel = new RefModel();
            refModel.setReference("Request");
            messageParameter.setSchema(refModel);
            //Adding conditional check for http delete operation as it cannot have body parameter.
            if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
                operationAdaptor.getOperation().addParameter(messageParameter);
            }
        }
    
        for (int i = 2; i < resource.getParameters().size(); i++) {
            VariableNode parameterDef = resource.getParameters().get(i);
            String typeName = parameterDef.getTypeNode().toString().toLowerCase(Locale.getDefault());
            PathParameter pathParameter = new PathParameter();
            // Set in value
            pathParameter.setIn("path");
            // Set parameter name
            String parameterName = parameterDef.getName().getValue();
            pathParameter.setName(parameterName);
            // Note: 'description' to be added using annotations, hence skipped here.
            // Note: 'allowEmptyValue' to be added using annotations, hence skipped here.
            // Set type
            if (typeName.contains("[]")) {
                pathParameter.setType("array");
                switch (typeName.replace("[]", "").trim()) {
                    case "string":
                        pathParameter.items(new StringProperty());
                        break;
                    case "int":
                        pathParameter.items(new IntegerProperty());
                        break;
                    case "boolean":
                        pathParameter.items(new BooleanProperty());
                        break;
                    default:
                        break;
                }
            } else if ("int".equals(typeName)) {
                pathParameter.setType("integer");
            } else {
                pathParameter.setType(typeName);
            }
            // Note: 'format' to be added using annotations, hence skipped here.
            operationAdaptor.getOperation().addParameter(pathParameter);
        }
    }

    /**
     * Create {@code Parameters} model for swagger operation.
     *
     * @param annotationExpression  The annotation attribute value for resource parameters
     * @param operation The swagger operation.
     */
    private void createParametersModel(BLangExpression annotationExpression, Operation operation) {
        if (annotationExpression != null) {
            List<Parameter> parameters = new LinkedList<>();
            List<? extends ExpressionNode> paramExprs = ((BLangArrayLiteral) annotationExpression).getExpressions();

            for (ExpressionNode expr : paramExprs) {
                List<BLangRecordKeyValue> paramList = ((BLangRecordLiteral) expr).getKeyValuePairs();
                Map<String, BLangExpression> paramAttributes = ConverterUtils.listToMap(paramList);
                String in;

                if (paramAttributes.containsKey("inInfo")) {
                    in = ConverterUtils.getStringLiteralValue(paramAttributes.get("inInfo"));
                } else {
                    // If parameter location is not provided. Place it as path param by default
                    in = "path";
                }

                Parameter pram = buildParameter(in);
                if (paramAttributes.containsKey("name")) {
                    pram.setName(ConverterUtils.getStringLiteralValue(paramAttributes.get("name")));
                }
                if (paramAttributes.containsKey("description")) {
                    pram.setDescription(ConverterUtils.getStringLiteralValue(paramAttributes.get("description")));
                }
                if (paramAttributes.containsKey("required")) {
                    pram.setRequired(Boolean.parseBoolean(
                            ConverterUtils.getStringLiteralValue(paramAttributes.get("required"))));
                }
                if (paramAttributes.containsKey("allowEmptyValue")) {
                    pram.setAllowEmptyValue(Boolean.parseBoolean(
                            ConverterUtils.getStringLiteralValue(paramAttributes.get("allowEmptyValue"))));
                }
                // TODO: 5/2/18 Set Param Schema Details

                parameters.add(pram);
            }

            operation.setParameters(parameters);
        }
    }

    /**
     * Parses the 'ResourceInfo' annotation and builds swagger operation.
     *
     * @param resource  The resource definition.
     * @param operation The swagger operation.
     */
    private void parseResourceInfoAnnotationAttachment(ResourceNode resource, Operation operation) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ResourceInfo", swaggerAlias,
                resource.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);
            this.createTagModel(attributes.get("tags"), operation);
            
            if (attributes.containsKey("summary")) {
                operation.setSummary(ConverterUtils.getStringLiteralValue(attributes.get("summary")));
            }
            if (attributes.containsKey("description")) {
                operation.setDescription(ConverterUtils.getStringLiteralValue(attributes.get("description")));
            }
            if (attributes.containsKey("parameters")) {
                this.createParametersModel(attributes.get("parameters"), operation);
            }

            this.createExternalDocsModel(attributes.get("externalDoc"), operation);
        }
    }

    /**
     * Creates external docs swagger definitions.
     *
     * @param annotationExpression The annotation attribute value for external docs.
     * @param operation                The swagger operation.
     */
    private void createExternalDocsModel(BLangExpression annotationExpression, Operation operation) {
        if (null != annotationExpression) {
            if (annotationExpression instanceof BLangRecordLiteral) {
                BLangRecordLiteral docAnnotation = (BLangRecordLiteral) annotationExpression;
                Map<String, BLangExpression> docAttrs =
                        ConverterUtils.listToMap(docAnnotation.getKeyValuePairs());
                ExternalDocs externalDocs = new ExternalDocs();

                if (docAttrs.containsKey("description")) {
                    externalDocs.setDescription(ConverterUtils.getStringLiteralValue(docAttrs.get("description")));
                }
                if (docAttrs.containsKey("url")) {
                    externalDocs.setUrl(ConverterUtils.getStringLiteralValue(docAttrs.get("url")));
                }
        
                operation.setExternalDocs(externalDocs);
            }
        }
    }

    /**
     * Creates tag model for swagger operation.
     *
     * @param annotationExpression The annotation expression value which has tags.
     * @param operation            The swagger operation.
     */
    private void createTagModel(BLangExpression annotationExpression, Operation operation) {
        if (null != annotationExpression) {
            List<? extends ExpressionNode> tagExprs = ((BLangArrayLiteral) annotationExpression).getExpressions();
            List<String> tags = new LinkedList<>();
            for (ExpressionNode expr : tagExprs) {
                if (expr instanceof BLangLiteral) {
                    BLangLiteral tagLit = (BLangLiteral) expr;
                    tags.add(ConverterUtils.getStringLiteralValue(tagLit));
                }
            }

            operation.setTags(tags);
        }
    }
    
    /**
     * Parse 'ResourceConfig' annotation attachment and build a resource operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The swagger operation.
     */
    private void parseResourceConfigAnnotationAttachment(ResourceNode resource, OperationAdaptor operation) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ResourceConfig", httpAlias,
                resource.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);

            if (attributes.containsKey("methods")) {

                // Setting default value is safe since empty 'methods' is handled separately by X-METHODS extension
                String method = "GET";
                BLangArrayLiteral methodsArray = (BLangArrayLiteral) attributes.get("methods");

                if (methodsArray.getExpressions().size() > 0) {
                    // Only one method is expected in this execution path
                    ExpressionNode expr = methodsArray.getExpressions().get(0);
                    BLangLiteral methodLit = (BLangLiteral) expr;

                    if (ConverterUtils.getStringLiteralValue(methodLit) != null) {
                        method = ConverterUtils.getStringLiteralValue(methodLit);
                    }
                }

                operation.setHttpOperation(method);
            }

            if (attributes.containsKey("path")) {
                operation.setPath(ConverterUtils.getStringLiteralValue(attributes.get("path")));
            }

            if (attributes.containsKey("consumes")) {
                List<String> consumes = new LinkedList<>();
                BLangArrayLiteral consumesArray = (BLangArrayLiteral) attributes.get("consumes");

                for (ExpressionNode expr : consumesArray.getExpressions()) {
                    BLangLiteral consumesLit = (BLangLiteral) expr;
                    String consumesVal = ConverterUtils.getStringLiteralValue(consumesLit);

                    if (consumesVal != null) {
                        consumes.add(consumesVal);
                    }
                }
                operation.getOperation().setConsumes(consumes);
            }

            if (attributes.containsKey("produces")) {
                List<String> produces = new LinkedList<>();
                BLangArrayLiteral producesArray = (BLangArrayLiteral) attributes.get("produces");

                for (ExpressionNode expr : producesArray.getExpressions()) {
                    BLangLiteral producesLit = (BLangLiteral) expr;
                    String producesVal = ConverterUtils.getStringLiteralValue(producesLit);

                    if (producesVal != null) {
                        produces.add(producesVal);
                    }
                }
                operation.getOperation().setProduces(produces);
            }
            // TODO: Implement security definitions.
            //this.createSecurityDefinitions(resourceConfigAnnotation.get().getAttributeNameValuePairs()
            // .get("authorizations"), operation);
        }
    }

    /**
     * Gets the http methods of a resource.
     *
     * @param resource    The ballerina resource.
     * @param useDefaults True to add default http methods, else false.
     * @return A list of http methods.
     */
    private List<String> getHttpMethods(ResourceNode resource, boolean useDefaults) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ResourceConfig", httpAlias,
                resource.getAnnotationAttachments());
        Set<String> httpMethods = new LinkedHashSet<>();
        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
            Map<String, BLangExpression> recordsMap = ConverterUtils.listToMap(list);
            if (recordsMap.containsKey("methods") && recordsMap.get("methods") != null) {
                List<? extends ExpressionNode> methodsValue = ((BLangArrayLiteral) recordsMap.get("methods"))
                        .getExpressions();
                for (ExpressionNode expr : methodsValue) {
                    httpMethods.add(ConverterUtils.getStringLiteralValue((BLangLiteral) expr));
                }

            }
        }
        if (httpMethods.isEmpty() && useDefaults) {
            // By default all http methods are supported.
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_GET);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_PUT);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_POST);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_DELETE);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_PATCH);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_OPTIONS);
            httpMethods.add("HEAD");
        }
        return Lists.reverse(new ArrayList<>(httpMethods));
    }

    /**
     * Gets the path value of the @http:resourceConfig.
     *
     * @param resource The ballerina resource.
     * @return The path value.
     */
    private String getPath(ResourceNode resource) {
        String path = "/" + resource.getName();
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("ResourceConfig", httpAlias,
                resource.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(list);

            if (attributes.containsKey("path")) {
                path = ConverterUtils.getStringLiteralValue(attributes.get("path"));
            }
        }
        
        return path;
    }

    /**
     * Builds a Swagger {@link Parameter} for provided parameter location.
     *
     * @param in location of the parameter in the request definition
     * @return Swagger {@link Parameter} for parameter location {@code in}
     */
    private Parameter buildParameter(String in) {
        Parameter param;

        switch (in) {
            case "body":
                param = new BodyParameter();
                break;
            case "query":
                param = new QueryParameter();
                break;
            case "header":
                param = new HeaderParameter();
                break;
            case "cookie":
                param = new CookieParameter();
                break;
            case "form":
                param = new FormParameter();
                break;
            case "path":
            default:
                param = new PathParameter();
        }

        return param;
    }

    /**
     * Retrieves a matching Swagger {@link Property} for a provided ballerina type.
     *
     * @param type ballerina type name as a String
     * @return Swagger {@link Property} for type defined by {@code type}
     */
    private Property getSwaggerProperty(String type) {
        Property property;

        switch (type) {
            case "string":
                property = new StringProperty();
                break;
            case "boolean":
                property = new BooleanProperty();
                break;
            case "array":
                property = new ArrayProperty();
                break;
            case "number":
            case "integer":
                property = new IntegerProperty();
                break;
            default:
                property = new ObjectProperty();
                break;
        }

        return property;
    }
}
