/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.openapi.validator.error.OpenapiServiceValidationError;
import org.ballerinalang.openapi.validator.error.ResourceValidationError;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This for finding out the all the filtered operations are documented as services in the ballerina file and all the
 * ballerina services are documented in the contract yaml file.
 */
public class ResourceWithOperationId {
    /**
     * Filter all the operations according to the given filters.
     * @param openApi       OpenApi Object
     * @param filters       Filter Object
     * @return              List of OpenApiPathSummary
     */
    public static List<OpenAPIPathSummary> filterOpenapi(OpenAPI openApi,
                                                         Filters filters) {

        boolean tagFilteringEnabled = filters.getTag().size() > 0;
        boolean operationFilteringEnabled = filters.getOperation().size() > 0;
        boolean excludeTagsFilteringEnabled = filters.getExcludeTag().size() > 0;
        boolean excludeOperationFilteringEnable = filters.getExcludeOperation().size() > 0;
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperationId.summarizeOpenAPI(openApi);
        // Check based on the method and path filters
        Iterator<OpenAPIPathSummary> openAPIIter = openAPIPathSummaries.iterator();
        while (openAPIIter.hasNext()) {
            OpenAPIPathSummary openAPIPathSummary = openAPIIter.next();
            // If operation filtering available proceed.
            // Else if proceed to check exclude operation filter is enable
            // Else check tag filtering or excludeTag filtering enable.
            if (operationFilteringEnabled) {
                // If tag filtering available validate only the filtered operations grouped by given tags.
                // Else if exclude tag filtering available validate only the operations that are not include
                // exclude Tags.
                // Else proceed only to validate filtered operations.
                if (tagFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        // Check operationId is null scenario.
                        // Check tag is available if it is null then remove other wise else-if not include
                        // tag then remove operations.
                        if (!(filters.getOperation().contains(operationMap.getValue().getOperationId())) ||
                                (operationMap.getValue().getOperationId() == null)) {
                                operations.remove();
                        } else {
                            if ((operationMap.getValue().getTags() == null) ||
                                    (Collections.disjoint(filters.getTag(), operationMap.getValue().getTags()))) {
                                operations.remove();
                            }
                        }
                    }
                } else if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (filters.getOperation().contains(operationMap.getValue().getOperationId())) {
                            //  Check tag is available
                            if ((operationMap.getValue().getTags() != null) && (!Collections.
                                    disjoint(filters.getExcludeTag(), operationMap.getValue().getTags()))) {
                                    operationIter.remove();
                            }
                        } else {
                            operationIter.remove();
                        }
                    }
                } else {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (!filters.getOperation().contains(operationMap.getValue().getOperationId())) {
                            operationIter.remove();
                        }
                    }
                }
            } else if (excludeOperationFilteringEnable) {
                // If exclude tags filtering available validate only the filtered exclude operations grouped by
                // given exclude tags.
                // Else If tags filtering available validate only the operations that filtered by exclude
                // operations.
                // Else proceed only to validate filtered exclude operations.
                if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if ((!filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) &&
                                ((operationMap.getValue().getTags() != null) && (!Collections
                                .disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())))) {
                                operationIter.remove();
                        }
                    }
                } else if (tagFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (!filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
                            //  Check tag is available if it is null and not included in list
                            //  then remove operations.
                            if ((operationMap.getValue().getTags() == null) || (Collections.disjoint(filters.getTag(),
                                    operationMap.getValue().getTags()))) {
                                operations.remove();
                            }
                        } else {
                            operations.remove();
                        }
                    }
                } else {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
                            operationIter.remove();
                        }
                    }
                }
                // If exclude tag filtering available proceed to validate all the operations grouped by tags which
                // are not included in list.
                // Else if validate the operations group by tag filtering
                // Else proceed without any filtering.
            } else {
                if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (operationMap.getValue().getTags() == null) {
                            break;
                        } else if (!Collections.disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())) {
                            operations.remove();
                        }
                    }
                } else if (tagFilteringEnabled) {
                    // If tag filtering available proceed to validate all the operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if ((operationMap.getValue().getTags() == null) || (Collections.disjoint(filters.getTag(),
                                operationMap.getValue().getTags()))) {
                            operations.remove();
                        }
                    }
                }
            }
            if (openAPIPathSummary.getOperations().isEmpty()) {
                openAPIIter.remove();
            }
        }
        return openAPIPathSummaries;
    }

    /**
     * Checking the available of resource function in openApi contract.
     * @param openAPI           openApi contract object
     * @param serviceNode       resource service node
     * @return                  validation Error list with ResourceValidationError type
     */
    public static List<ResourceValidationError> checkOperationIsAvailable(OpenAPI openAPI, ServiceNode serviceNode) {
        List<ResourceValidationError> resourceValidationErrorList = new ArrayList<>();
        List<ResourcePathSummary> resourcePathSummaries = summarizeResources(serviceNode);
        List<OpenAPIPathSummary> openAPISummaries = summarizeOpenAPI(openAPI);
        // Check given path with its methods has documented in OpenApi contract
        for (ResourcePathSummary resourcePathSummary: resourcePathSummaries) {
            boolean isExit = false;
            String resourcePath = resourcePathSummary.getPath();
            Map<String, ResourceMethod> resourcePathMethods = resourcePathSummary.getMethods();
            for (OpenAPIPathSummary openAPIPathSummary : openAPISummaries) {
                String servicePath = openAPIPathSummary.getPath();
                List<String> servicePathOperations = openAPIPathSummary.getAvailableOperations();
                if (resourcePath.equals(servicePath)) {
                    isExit = true;
                    if ((!servicePathOperations.isEmpty()) && (!resourcePathMethods.isEmpty())) {
                        for (Map.Entry<String, ResourceMethod> entry : resourcePathMethods.entrySet()) {
                            boolean isMethodExit = false;
                            for (String operation : servicePathOperations) {
                                if (entry.getKey().equals(operation)) {
                                    isMethodExit = true;
                                    break;
                                }
                            }
                            if (!isMethodExit) {
                                ResourceValidationError resourceValidationError =
                                        new ResourceValidationError(entry.getValue().getMethodPosition(),
                                                entry.getKey(), resourcePath);
                                resourceValidationErrorList.add(resourceValidationError);
                            }
                        }
                    }
                    break;
                }
            }
            if (!isExit) {
                ResourceValidationError resourceValidationError =
                        new ResourceValidationError(resourcePathSummary.getPathPosition(), null, resourcePath);
                resourceValidationErrorList.add(resourceValidationError);
            }
        }
        return resourceValidationErrorList;
    }

    /**
     * Checking the documented services are available at the resource file.
     * @param openAPISummaries  openApi contract object
     * @param serviceNode       resource file service
     * @return                  validation error list type with OpenAPIServiceValidationError
     */
    public static List<OpenapiServiceValidationError> checkServiceAvailable(List<OpenAPIPathSummary> openAPISummaries,
                                                                            ServiceNode serviceNode) {
        List<OpenapiServiceValidationError> validationErrors = new ArrayList<>();
        List<ResourcePathSummary> resourcePathSummaries = summarizeResources(serviceNode);
        // check the contract paths are available at the resource
        for (OpenAPIPathSummary openAPIPathSummary: openAPISummaries) {
            boolean isServiceExit = false;
            for (ResourcePathSummary resourcePathSummary: resourcePathSummaries) {
                if (openAPIPathSummary.getPath().equals(resourcePathSummary.getPath())) {
                    isServiceExit = true;
                    //  check whether documented operations are available at resource file
                    if (!openAPIPathSummary.getAvailableOperations().isEmpty()) {
                        for (String operation: openAPIPathSummary.getAvailableOperations()) {
                            boolean isOperationExit = false;
                            if (!(resourcePathSummary.getMethods().isEmpty())) {
                                for (Map.Entry<String, ResourceMethod> method:
                                        resourcePathSummary.getMethods().entrySet()) {
                                    if (operation.equals(method.getKey())) {
                                        isOperationExit = true;
                                        break;
                                    }
                                }
                                if (!isOperationExit) {
                                    OpenapiServiceValidationError openapiServiceValidationError =
                                            new OpenapiServiceValidationError(serviceNode.getPosition(), operation,
                                                    openAPIPathSummary.getPath(),
                                                    openAPIPathSummary.getOperations().get(operation).getTags(),
                                                    openAPIPathSummary);
                                    validationErrors.add(openapiServiceValidationError);
                                }
                            }
                        }
                    }
                    break;
                }
            }
            if (!isServiceExit) {
                OpenapiServiceValidationError openapiServiceValidationError =
                        new OpenapiServiceValidationError(serviceNode.getPosition(),
                                null, openAPIPathSummary.getPath(), null, openAPIPathSummary);
                validationErrors.add(openapiServiceValidationError);
            }
        }
        return validationErrors;
    }

    /**
     * Extract the details to be validated from the resource.
     * @param serviceNode         service node
     * @return List of ResourcePathSummary
     */
    public static List<ResourcePathSummary>  summarizeResources(ServiceNode serviceNode) {
        // Iterate resources available in a service and extract details to be validated.
        List<ResourcePathSummary> resourceSummaryList = new ArrayList<>();
        for (FunctionNode resource : serviceNode.getResources()) {
            AnnotationAttachmentNode annotation = null;
            // Find the "ResourceConfig" annotation.
            for (AnnotationAttachmentNode ann : resource.getAnnotationAttachments()) {
                if (Constants.HTTP.equals(ann.getPackageAlias().getValue())
                        && Constants.RESOURCE_CONFIG.equals(ann.getAnnotationName().getValue())) {
                    annotation = ann;
                }
            }
            if (annotation != null) {
                if (annotation.getExpression() instanceof BLangRecordLiteral) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();
                    String methodPath = null;
                    Diagnostic.DiagnosticPosition pathPos = null;
                    ResourceMethod resourceMethod = new ResourceMethod();
                    String methodName = null;
                    Diagnostic.DiagnosticPosition methodPos = null;
                    String body = null;
                    for (BLangRecordLiteral.RecordField field : recordLiteral.getFields()) {
                        BLangExpression keyExpr;
                        BLangExpression valueExpr;
                        if (field.isKeyValueField()) {
                            BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                            keyExpr = keyValue.getKey();
                            valueExpr = keyValue.getValue();
                        } else {
                            BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                    (BLangRecordLiteral.BLangRecordVarNameField) field;
                            keyExpr = varNameField;
                            valueExpr = varNameField;
                        }
                        if (keyExpr instanceof BLangSimpleVarRef) {
                            BLangSimpleVarRef path = (BLangSimpleVarRef) keyExpr;
                            String contractAttr = path.getVariableName().getValue();
                            // Extract the path and methods of the resource.
                            if (contractAttr.equals(Constants.PATH)) {
                                if (valueExpr instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) valueExpr;
                                    if (value.getValue() instanceof String) {
                                        methodPath = (String) value.getValue();
                                        pathPos = path.getPosition();
                                    }
                                }
                            } else if (contractAttr.equals(Constants.METHODS) ||
                                    contractAttr.equals(Constants.METHOD)) {

                                if (valueExpr instanceof BLangListConstructorExpr) {
                                    BLangListConstructorExpr methodSet = (BLangListConstructorExpr) valueExpr;
                                    for (BLangExpression methodExpr : methodSet.exprs) {
                                        if (methodExpr instanceof BLangLiteral) {
                                            BLangLiteral method = (BLangLiteral) methodExpr;
                                            methodName = ((String) method.value).toLowerCase(Locale.ENGLISH);
                                            methodPos = path.getPosition();

                                        }
                                    }
                                }
                            } else if (contractAttr.equals(Constants.BODY)) {
                                if (valueExpr instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) valueExpr;
                                    if (value.getValue() instanceof String) {
                                        body = (String) value.getValue();
                                    }
                                }
                            }
                        }
                    }
                    Boolean isPathExit = false;
                    if (!resourceSummaryList.isEmpty()) {
                        for (ResourcePathSummary resourcePathSummary1 : resourceSummaryList) {
                            isPathExit = false;
                            if (methodPath != null) {
                                if (methodPath.equals(resourcePathSummary1.getPath())) {
                                    setValuesResourceMethods(resource, resourceMethod, methodName, methodPos, body,
                                            resourcePathSummary1);
                                    isPathExit = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!isPathExit) {

                        ResourcePathSummary resourcePathSummary = new ResourcePathSummary();
                        resourcePathSummary.setPath(methodPath);
                        resourcePathSummary.setPathPosition(pathPos);
                        setValuesResourceMethods(resource, resourceMethod, methodName, methodPos, body,
                                resourcePathSummary);
                        // Add the resource summary to the resource summary list.
                        resourceSummaryList.add(resourcePathSummary);
                    }
                }
            }
        }
        return resourceSummaryList;
    }

    private static void setValuesResourceMethods(FunctionNode resource, ResourceMethod resourceMethod,
                                                 String methodName, Diagnostic.DiagnosticPosition methodPos,
                                                 String body, ResourcePathSummary resourcePathSummary) {

        if (body != null) {
            resourceMethod.setBody(body);
        }
        // Extract and add the resource parameters
        if (resource.getParameters().size() > 0) {
            resourceMethod.setParameters(resource.getParameters());
        }
        if (methodName != null) {
            resourceMethod.setMethod(methodName);
        }
        if (methodPos != null) {
            resourceMethod.setMethodPosition(methodPos);
        }
        if (resource.getPosition() != null) {
            resourceMethod.setResourcePosition(resource.getPosition());
        }
        resourcePathSummary.addMethod(methodName, resourceMethod);
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     * @param contract                openAPI contract
     * @return List of summarized OpenAPIPathSummary
     */
    public static List<OpenAPIPathSummary>   summarizeOpenAPI(OpenAPI contract) {
        List<OpenAPIPathSummary> openAPISummaries = new ArrayList<>();
        io.swagger.v3.oas.models.Paths paths = contract.getPaths();
        for (Map.Entry pathItem : paths.entrySet()) {
            OpenAPIPathSummary openAPISummary = new OpenAPIPathSummary();
            if (pathItem.getKey() instanceof String
                    && pathItem.getValue() instanceof PathItem) {
                String key = (String) pathItem.getKey();
                openAPISummary.setPath(key);

                PathItem operations = (PathItem) pathItem.getValue();
                if (operations.getGet() != null) {
                    addOpenapiSummary(openAPISummary, Constants.GET, operations.getGet());
                }
                if (operations.getPost() != null) {
                    addOpenapiSummary(openAPISummary, Constants.POST, operations.getPost());
                }
                if (operations.getPut() != null) {
                    addOpenapiSummary(openAPISummary, Constants.PUT, operations.getPut());
                }
                if (operations.getDelete() != null) {
                    addOpenapiSummary(openAPISummary, Constants.DELETE, operations.getDelete());
                }
                if (operations.getHead() != null) {
                    addOpenapiSummary(openAPISummary, Constants.HEAD, operations.getHead());
                }
                if (operations.getPatch() != null) {
                    addOpenapiSummary(openAPISummary, Constants.PATCH, operations.getPatch());
                }

                if (operations.getOptions() != null) {
                    addOpenapiSummary(openAPISummary, Constants.OPTIONS, operations.getOptions());
                }
                if (operations.getTrace() != null) {
                    addOpenapiSummary(openAPISummary, Constants.TRACE, operations.getTrace());
                }
            }
            openAPISummaries.add(openAPISummary);
        }
    return openAPISummaries;
    }

    private static void addOpenapiSummary(OpenAPIPathSummary openAPISummary, String get, Operation get2) {
        openAPISummary.addAvailableOperation(get);
        openAPISummary.addOperation(get, get2);
    }

    /**
     * Remove operations based on the missing errors.
     * @param openAPISummaries      List of OpenApiPathSummary
     * @param missingPathInResource Error List of missing operation
     * @return Filtered List with OpenAPiPathSummary
     */
    public static List<OpenAPIPathSummary> removeUndocumentedPath(List<OpenAPIPathSummary> openAPISummaries,
                                                                  List<OpenapiServiceValidationError>
                                                                          missingPathInResource) {
        if (!openAPISummaries.isEmpty()) {
            Iterator<OpenAPIPathSummary> openAPIPathIterator = openAPISummaries.iterator();
            while (openAPIPathIterator.hasNext()) {
                OpenAPIPathSummary openAPIPathSummary = openAPIPathIterator.next();
                if (!missingPathInResource.isEmpty()) {
                    for (OpenapiServiceValidationError error: missingPathInResource) {
                        if (error.getServicePath().equals(openAPIPathSummary.getPath())) {
                            if ((error.getServiceOperation() != null) &&
                                    (!openAPIPathSummary.getOperations().isEmpty())) {
                                Map<String, Operation> operationsMap = openAPIPathSummary.getOperations();
                                operationsMap.entrySet().removeIf(operationMap -> operationMap.getKey()
                                        .equals(error.getServiceOperation()));
                            } else if (error.getServiceOperation() == null) {
                                openAPIPathIterator.remove();
                            }
                        }
                    }
                }
            }
        }
        return openAPISummaries;
    }
}
