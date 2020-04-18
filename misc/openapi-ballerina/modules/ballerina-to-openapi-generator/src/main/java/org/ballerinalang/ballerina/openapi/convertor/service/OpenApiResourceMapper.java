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

package org.ballerinalang.ballerina.openapi.convertor.service;

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
import org.ballerinalang.ballerina.openapi.convertor.ConverterUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;

/**
 * This class will do resource mapping from ballerina to openApi.
 */
public class OpenApiResourceMapper {
    private final String httpAlias;
    private final String openApiAlias;
    private final Swagger openApiDefinition;

    /**
     * Initializes a resource parser for openApi.
     *
     * @param openApi      The OpenAPI definition.
     * @param httpAlias    The alias for ballerina/http module.
     * @param openApiAlias The alias for ballerina.openapi module.
     */
    OpenApiResourceMapper(Swagger openApi, String httpAlias, String openApiAlias) {
        this.httpAlias = httpAlias;
        this.openApiAlias = openApiAlias;
        this.openApiDefinition = openApi;
    }

    /**
     * This method will convert ballerina resource to openApi path objects.
     *
     * @param resources Resource array to be convert.
     * @return map of string and openApi path objects.
     */
    protected Map<String, Path> convertResourceToPath(List<BLangFunction> resources) {
        Map<String, Path> pathMap = new HashMap<>();
        for (BLangFunction resource : resources) {
            List<String> methods = this.getHttpMethods(resource, false);
            if (methods.size() == 0
                    || methods.size() > 1) {
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
    private void useMultiResourceMapper(Map<String, Path> pathMap, BLangFunction resource) {
        List<String> httpMethods = this.getHttpMethods(resource, false);
        String path = this.getPath(resource);
        Path pathObject = new Path();
        Operation operation;
        if (httpMethods.size() > 1) {
            int i = 1;
            for (String httpMethod : httpMethods) {
                //Iterate through http methods and fill path map.
                operation = this.convertResourceToOperation(resource, httpMethod, i).getOperation();
                pathObject.set(httpMethod.toLowerCase(Locale.ENGLISH), operation);
                i++;
            }
        }
        pathMap.put(path, pathObject);
    }

    /**
     * Resource mapper when a resource has only one http method.
     *
     * @param pathMap  The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useDefaultResourceMapper(Map<String, Path> pathMap, BLangFunction resource) {
        String httpMethod = getHttpMethods(resource, true).get(0);
        OperationAdaptor operationAdaptor = this.convertResourceToOperation(resource, httpMethod, 1);
        operationAdaptor.setHttpOperation(httpMethod);
        Path path = pathMap.get(operationAdaptor.getPath());
        if (path == null) {
            path = new Path();
            pathMap.put(operationAdaptor.getPath(), path);
        }
        Operation operation = operationAdaptor.getOperation();
        switch (httpMethod) {
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
    private OperationAdaptor convertResourceToOperation(BLangFunction resource, String httpMethod, int idIncrement) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            op.setHttpOperation(httpMethod);
            op.setPath('/' + resource.getName().getValue());
            Response response = new Response()
                    .description("Successful")
                    .example(MediaType.APPLICATION_JSON, "Ok");
            op.getOperation().response(200, response);

            // Replacing all '_' with ' ' to keep the consistency with what we are doing in openApi -> bal
            // @see BallerinaOperation#buildContext
            String resName = resource.getName().getValue().replaceAll("_", " ");
            op.getOperation().setOperationId(getOperationId(idIncrement, resName));
            op.getOperation().setParameters(null);

            // Parsing annotations.
            this.parseResourceConfigAnnotationAttachment(resource, op);
            this.parseResourceInfo(resource, op.getOperation(), httpMethod);
            this.addResourceParameters(resource, op);
            this.parseResponsesAnnotationAttachment(resource, op.getOperation());
        }
        return op;
    }

    /**
     * get UUID generated with the given post fix.
     *
     * @param postFix string post fix to attach to ID
     * @return {@link String} generated UUID
     */
    private String getOperationId(int idIncrement, String postFix) {
        return "operation" + idIncrement + "_" + postFix;
    }

    /**
     * Parses the 'Responses' annotation attachment and build openApi operation.
     *
     * @param resource The ballerina resource definition.
     * @param op       The openApi operation.
     */
    private void parseResponsesAnnotationAttachment(BLangFunction resource, Operation op) {
        AnnotationAttachmentNode annotation = ConverterUtils.getAnnotationFromList("Responses", openApiAlias,
                resource.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            Map<String, BLangExpression> attrs = ConverterUtils.listToMap(bLiteral.getFields());

            if (attrs.containsKey(ConverterConstants.ATTR_VALUE)) {
                BLangListConstructorExpr valueArr = (BLangListConstructorExpr) attrs.get(ConverterConstants.ATTR_VALUE);

                if (valueArr.getExpressions().size() > 0) {
                    Map<String, Response> responses = new HashMap<>();

                    for (ExpressionNode expr : valueArr.getExpressions()) {
                        Map<String, BLangExpression> attributes =
                                ConverterUtils.listToMap(((BLangRecordLiteral) expr).getFields());

                        if (attributes.containsKey(ConverterConstants.ATTR_CODE)) {
                            String code = ConverterUtils
                                    .getStringLiteralValue(attributes.get(ConverterConstants.ATTR_CODE));
                            Response response = new Response();
                            if (attributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                                response.setDescription(ConverterUtils
                                        .getStringLiteralValue(attributes.get(ConverterConstants.ATTR_DESCRIPTION)));
                            }
                            // TODO: Parse 'response' attribute for $.paths./resource-path.responses[*]["code"].schema
                            this.createHeadersModel(attributes.get(ConverterConstants.ATTR_HEADERS), response);
                            responses.put(code, response);
                        }
                    }
                    op.setResponses(responses);
                }
            }
        }
    }

    /**
     * Creates headers definitions for openApi response.
     *
     * @param annotationExpression The annotation attribute value which has the headers.
     * @param response             The openApi response.
     */
    private void createHeadersModel(BLangExpression annotationExpression, Response response) {
        if (null != annotationExpression) {
            BLangListConstructorExpr headerArray = (BLangListConstructorExpr) annotationExpression;

            for (ExpressionNode headersValue : headerArray.getExpressions()) {
                Map<String, BLangExpression> headersAttributes =
                        ConverterUtils.listToMap(((BLangRecordLiteral) headersValue).getFields());
                Map<String, Property> headers = new HashMap<>();

                if (headersAttributes.containsKey(ConverterConstants.ATTR_NAME) && headersAttributes
                        .containsKey(ConverterConstants.ATTR_HEADER_TYPE)) {
                    String headerName = ConverterUtils
                            .getStringLiteralValue(headersAttributes.get(ConverterConstants.ATTR_NAME));
                    String type = ConverterUtils
                            .getStringLiteralValue(headersAttributes.get(ConverterConstants.ATTR_HEADER_TYPE));
                    Property property = getOpenApiProperty(type);

                    if (headersAttributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                        property.setDescription(ConverterUtils
                                .getStringLiteralValue(headersAttributes.get(ConverterConstants.ATTR_DESCRIPTION)));
                    }
                    headers.put(headerName, property);
                }
                response.setHeaders(headers);
            }
        }
    }

    /**
     * Creates parameters in the openApi operation using the parameters in the ballerina resource definition.
     *
     * @param resource         The ballerina resource definition.
     * @param operationAdaptor The openApi operation.
     */
    private void addResourceParameters(BLangFunction resource, OperationAdaptor operationAdaptor) {
        //Set Path
        AnnotationAttachmentNode annotation = ConverterUtils
                .getAnnotationFromList(HttpConstants.ANN_NAME_RESOURCE_CONFIG, httpAlias,
                        resource.getAnnotationAttachments());

        if (annotation != null) {
            Map<String, BLangExpression> recordsMap =
                    ConverterUtils.listToMap(((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                            .getExpression()).getFields());

            if (recordsMap.containsKey(HttpConstants.ANN_RESOURCE_ATTR_PATH)
                    && recordsMap.get(HttpConstants.ANN_RESOURCE_ATTR_PATH) != null) {
                String path = recordsMap.get(HttpConstants.ANN_RESOURCE_ATTR_PATH).toString().trim();
                operationAdaptor.setPath(path);
            } else {
                operationAdaptor.setPath("/");
            }
        }

        //Add path parameters if in path
        if (resource.requiredParams.size() > 0) {
            List requiredParams = resource.requiredParams;
            for (Object parameter : requiredParams) {
                BLangSimpleVariable param = (BLangSimpleVariable) parameter;
                boolean isCaller = ((param.typeNode instanceof BLangUserDefinedType) &&
                        ((BLangUserDefinedType) param.typeNode).typeName.value.equals("Caller"));
                boolean isRequest = ((param.typeNode instanceof BLangUserDefinedType) &&
                        ((BLangUserDefinedType) param.typeNode).typeName.value.equals("Request"));
                if (!isCaller && !isRequest) {
                    PathParameter pathParameter = new PathParameter();
                    pathParameter.setName(param.getName().value);
                    pathParameter.setType(param.type.tsymbol.name.value);
                    operationAdaptor.getOperation().addParameter(pathParameter);
                }
            }
        }


        if (!"get".equalsIgnoreCase(operationAdaptor.getHttpOperation())) {

            // Creating request body - required.
            ModelImpl messageModel = new ModelImpl();
            messageModel.setType("object");
            Map<String, Model> definitions = new HashMap<>();
            if (!definitions.containsKey(ConverterConstants.ATTR_REQUEST)) {
                definitions.put(ConverterConstants.ATTR_REQUEST, messageModel);
                this.openApiDefinition.setDefinitions(definitions);
            }

            // Creating "Request rq" parameter
            BodyParameter messageParameter = new BodyParameter();
            messageParameter.setName(resource.getParameters().get(0).getName().getValue());
            RefModel refModel = new RefModel();
            refModel.setReference(ConverterConstants.ATTR_REQUEST);
            messageParameter.setSchema(refModel);
            //Adding conditional check for http delete operation as it cannot have body parameter.
            if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
                operationAdaptor.getOperation().addParameter(messageParameter);
            }
        }

    }

    /**
     * Create {@code Parameters} model for openApi operation.
     *
     * @param annotationExpression The annotation attribute value for resource parameters
     * @param operation            The openApi operation.
     */
    private void createParametersModel(BLangExpression annotationExpression, Operation operation) {
        if (annotationExpression != null) {
            List<Parameter> parameters = new LinkedList<>();
            List<? extends ExpressionNode> paramExprs = ((BLangListConstructorExpr) annotationExpression)
                    .getExpressions();

            for (ExpressionNode expr : paramExprs) {
                Map<String, BLangExpression> paramAttributes =
                        ConverterUtils.listToMap(((BLangRecordLiteral) expr).getFields());
                String in;

                if (paramAttributes.containsKey(ConverterConstants.ATTR_IN)) {
                    in = ConverterUtils.getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_IN));
                } else {
                    // If parameter location is not provided. Place it as path param by default
                    in = "path";
                }

                Parameter param = buildParameter(in, paramAttributes);
                if (paramAttributes.containsKey(ConverterConstants.ATTR_NAME)) {
                    param.setName(
                            ConverterUtils.getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_NAME)));
                }
                if (paramAttributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                    param.setDescription(ConverterUtils
                            .getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_DESCRIPTION)));
                }
                if (paramAttributes.containsKey(ConverterConstants.ATTR_REQUIRED)) {
                    param.setRequired(Boolean.parseBoolean(ConverterUtils
                            .getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_REQUIRED))));
                }
                // TODO: 5/2/18 Set Param Schema Details

                parameters.add(param);
            }

            operation.setParameters(parameters);
        }
    }

    /**
     * Parses the 'ResourceInfo' annotation and builds openApi operation.
     *
     * @param resource  The resource definition.
     * @param operation The openApi operation.
     */
    private void parseResourceInfo(BLangFunction resource, Operation operation, String httpMethod) {
        AnnotationAttachmentNode multiResourceInfoAnnotation = ConverterUtils
                .getAnnotationFromList(ConverterConstants.ANNON_MULTI_RES_INFO, openApiAlias,
                        resource.getAnnotationAttachments());
        if (multiResourceInfoAnnotation != null) {
            parseMultiResourceInfoAnnotationAttachment(multiResourceInfoAnnotation, operation, httpMethod);
        } else {
            AnnotationAttachmentNode annotation = ConverterUtils
                    .getAnnotationFromList(ConverterConstants.ANNON_RES_INFO, openApiAlias,
                            resource.getAnnotationAttachments());

            if (annotation != null) {
                BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                        .getExpression());
                addResourceInfoToOperation(bLiteral, operation);
            }
        }
    }

    private void parseMultiResourceInfoAnnotationAttachment(AnnotationAttachmentNode multiResourceInfoAnnotation,
                                                            Operation operation, String httpMethod) {
        // Get multi resource information
        if (multiResourceInfoAnnotation != null) {
            BLangRecordLiteral bLiteral = (BLangRecordLiteral) ((BLangAnnotationAttachment) multiResourceInfoAnnotation)
                    .getExpression();
            // In multi resource information there is only one key exist that is `resource information`.
            BLangRecordKeyValueField resourceInformationAttr = bLiteral.fields.size() == 1
                    ? (BLangRecordKeyValueField) bLiteral.fields.get(0)
                    : null;
            if (resourceInformationAttr != null) {
                for (RecordLiteralNode.RecordField resourceInfo :
                        ((BLangRecordLiteral) resourceInformationAttr.valueExpr).getFields()) {
                    BLangRecordKeyValueField resourceInfoKeyValue = (BLangRecordKeyValueField) resourceInfo;
                    if (((BLangLiteral) resourceInfoKeyValue.key.expr).value.equals(httpMethod)) {
                        addResourceInfoToOperation(((BLangRecordLiteral) resourceInfoKeyValue.valueExpr), operation);
                    }
                }
            }
        }
    }

    private void addResourceInfoToOperation(BLangRecordLiteral bLiteral, Operation operation) {
        Map<String, BLangExpression> attributes = ConverterUtils.listToMap(bLiteral.getFields());
        this.createTagModel(attributes.get(ConverterConstants.ATTR_TAGS), operation);

        if (attributes.containsKey(ConverterConstants.ATTR_SUMMARY)) {
            operation.setSummary(
                    ConverterUtils.getStringLiteralValue(attributes.get(ConverterConstants.ATTR_SUMMARY)));
        }
        if (attributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
            operation.setDescription(
                    ConverterUtils.getStringLiteralValue(attributes.get(ConverterConstants.ATTR_DESCRIPTION)));
        }
        if (attributes.containsKey(ConverterConstants.ATTR_PARAM)) {
            this.createParametersModel(attributes.get(ConverterConstants.ATTR_PARAM), operation);
        }

        this.createExternalDocsModel(attributes.get(ConverterConstants.ATTR_EXT_DOC), operation);
    }

    /**
     * Creates external docs openApi definitions.
     *
     * @param annotationExpression The annotation attribute value for external docs.
     * @param operation            The openApi operation.
     */
    private void createExternalDocsModel(BLangExpression annotationExpression, Operation operation) {
        if (null != annotationExpression) {
            if (annotationExpression instanceof BLangRecordLiteral) {
                BLangRecordLiteral docAnnotation = (BLangRecordLiteral) annotationExpression;
                Map<String, BLangExpression> docAttrs = ConverterUtils.listToMap(docAnnotation.getFields());
                ExternalDocs externalDocs = new ExternalDocs();

                if (docAttrs.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                    externalDocs.setDescription(
                            ConverterUtils.getStringLiteralValue(docAttrs.get(ConverterConstants.ATTR_DESCRIPTION)));
                }
                if (docAttrs.containsKey(ConverterConstants.ATTR_URL)) {
                    externalDocs
                            .setUrl(ConverterUtils.getStringLiteralValue(docAttrs.get(ConverterConstants.ATTR_URL)));
                }

                operation.setExternalDocs(externalDocs);
            }
        }
    }

    /**
     * Creates tag model for openApi operation.
     *
     * @param annotationExpression The annotation expression value which has tags.
     * @param operation            The openApi operation.
     */
    private void createTagModel(BLangExpression annotationExpression, Operation operation) {
        if (null != annotationExpression) {
            List<? extends ExpressionNode> tagExprs = ((BLangListConstructorExpr) annotationExpression)
                    .getExpressions();
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
     * @param operation The openApi operation.
     */
    private void parseResourceConfigAnnotationAttachment(BLangFunction resource, OperationAdaptor operation) {
        AnnotationAttachmentNode annotation = ConverterUtils
                .getAnnotationFromList(HttpConstants.ANN_NAME_RESOURCE_CONFIG, httpAlias,
                        resource.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(bLiteral.getFields());

            if (attributes.containsKey(HttpConstants.ANN_RESOURCE_ATTR_PATH)) {
                operation.setPath(
                        ConverterUtils.getStringLiteralValue(attributes.get(HttpConstants.ANN_RESOURCE_ATTR_PATH)));
            }

            if (attributes.containsKey(HttpConstants.ANN_RESOURCE_ATTR_CONSUMES)) {
                List<String> consumes = new LinkedList<>();
                BLangListConstructorExpr consumesArray = (BLangListConstructorExpr) attributes
                        .get(HttpConstants.ANN_RESOURCE_ATTR_CONSUMES);

                for (ExpressionNode expr : consumesArray.getExpressions()) {
                    if (expr instanceof BLangLiteral) {
                        BLangLiteral consumesLit = (BLangLiteral) expr;
                        String consumesVal = ConverterUtils.getStringLiteralValue(consumesLit);

                        if (consumesVal != null) {
                            consumes.add(consumesVal);
                        }
                    }
                }
                operation.getOperation().setConsumes(consumes);
            }

            if (attributes.containsKey(HttpConstants.ANN_RESOURCE_ATTR_PRODUCES)) {
                List<String> produces = new LinkedList<>();
                BLangListConstructorExpr producesArray = (BLangListConstructorExpr) attributes
                        .get(HttpConstants.ANN_RESOURCE_ATTR_PRODUCES);

                for (ExpressionNode expr : producesArray.getExpressions()) {
                    if (expr instanceof BLangLiteral) {
                        BLangLiteral producesLit = (BLangLiteral) expr;
                        String producesVal = ConverterUtils.getStringLiteralValue(producesLit);

                        if (producesVal != null) {
                            produces.add(producesVal);
                        }
                    }
                }
                operation.getOperation().setProduces(produces);
            }
            // TODO: Implement security definitions.
            //this.createSecurityDefinitions(resourceConfigAnnotation.get().getAttributeNameValuePairs()
            // .get("authorizations"), operation);
        } else {
            operation.setPath(resource.getName().getValue());
        }
    }

    /**
     * Gets the http methods of a resource.
     *
     * @param resource    The ballerina resource.
     * @param useDefaults True to add default http methods, else false.
     * @return A list of http methods.
     */
    private List<String> getHttpMethods(BLangFunction resource, boolean useDefaults) {
        AnnotationAttachmentNode annotation = ConverterUtils
                .getAnnotationFromList(HttpConstants.ANN_NAME_RESOURCE_CONFIG, httpAlias,
                        resource.getAnnotationAttachments());
        Set<String> httpMethods = new LinkedHashSet<>();

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            Map<String, BLangExpression> recordsMap = ConverterUtils.listToMap(bLiteral.getFields());
            if (recordsMap.containsKey(HttpConstants.ANN_RESOURCE_ATTR_METHODS)
                    && recordsMap.get(HttpConstants.ANN_RESOURCE_ATTR_METHODS) != null) {
                List<? extends ExpressionNode> methodsValue = ((BLangListConstructorExpr) recordsMap
                        .get(HttpConstants.ANN_RESOURCE_ATTR_METHODS)).getExpressions();
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
    private String getPath(BLangFunction resource) {
        String path = "/" + resource.getName();
        AnnotationAttachmentNode annotation = ConverterUtils
                .getAnnotationFromList(HttpConstants.ANN_NAME_RESOURCE_CONFIG, httpAlias,
                        resource.getAnnotationAttachments());

        if (annotation != null) {
            BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) annotation)
                    .getExpression());
            Map<String, BLangExpression> attributes = ConverterUtils.listToMap(bLiteral.getFields());

            if (attributes.containsKey(HttpConstants.ANN_RESOURCE_ATTR_PATH)) {
                path = ConverterUtils.getStringLiteralValue(attributes.get(HttpConstants.ANN_RESOURCE_ATTR_PATH));
            }
        }

        return path;
    }

    /**
     * Builds an OpenApi {@link Parameter} for provided parameter location.
     *
     * @param in              location of the parameter in the request definition
     * @param paramAttributes parameter attributes for the operation
     * @return OpenApi {@link Parameter} for parameter location {@code in}
     */
    private Parameter buildParameter(String in, Map<String, BLangExpression> paramAttributes) {
        Parameter param;

        switch (in) {
            case "body":
                // TODO : support for inline and other types of schemas
                BodyParameter bParam = new BodyParameter();
                RefModel m = new RefModel();
                m.set$ref(ConverterUtils
                        .getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_TYPE)));
                bParam.setSchema(m);
                param = bParam;
                break;
            case "query":
                QueryParameter qParam = new QueryParameter();
                String attrType = ConverterUtils
                        .getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_TYPE)).trim();
                String type;
                switch (attrType) {
                    case "int":
                        type = "integer";
                        break;
                    case "float":
                        type = "number";
                        break;
                    default:
                        type = attrType;
                        break;
                }
                qParam.setType(type);
                param = qParam;
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
                PathParameter pParam = new PathParameter();
                pParam.setType(ConverterUtils
                        .getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_TYPE)));
                param = pParam;
        }

        return param;
    }

    /**
     * Retrieves a matching OpenApi {@link Property} for a provided ballerina type.
     *
     * @param type ballerina type name as a String
     * @return OpenApi {@link Property} for type defined by {@code type}
     */
    private Property getOpenApiProperty(String type) {
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
