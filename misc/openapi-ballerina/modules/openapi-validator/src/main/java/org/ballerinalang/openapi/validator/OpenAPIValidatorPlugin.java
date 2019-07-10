/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Compiler plugin for ballerina OpenAPI/service validator.
 */
@SupportedAnnotationPackages(value = {"ballerina/openapi"})
public class OpenAPIValidatorPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dLog = null;
    private List<ResourceSummary> resourceSummaryList;
    private List<OpenAPIPathSummary> openAPISummaryList;
    private OpenAPIComponentSummary openAPIComponentSummary;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dLog = diagnosticLog;
        this.resourceSummaryList = new ArrayList<>();
        this.openAPISummaryList = new ArrayList<>();
        this.openAPIComponentSummary = new OpenAPIComponentSummary();
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode annotation = null;
        List<String> tags = new ArrayList<>();
        List<String> operations = new ArrayList<>();
        this.openAPIComponentSummary = new OpenAPIComponentSummary();
        String contractURI = null;

        for (AnnotationAttachmentNode ann : annotations) {
            if (Constants.PACKAGE.equals(ann.getPackageAlias().getValue())
                    && Constants.ANNOTATION_NAME.equals(ann.getAnnotationName().getValue())) {
                annotation = ann;
            }
        }

        if (annotation != null) {
            if (annotation.getExpression() instanceof BLangRecordLiteral) {
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();
                for (BLangRecordLiteral.BLangRecordKeyValue keyValue : recordLiteral.getKeyValuePairs()) {
                    if (keyValue.getKey() instanceof BLangSimpleVarRef) {
                        BLangSimpleVarRef contract = (BLangSimpleVarRef) keyValue.getKey();
                        String key = contract.getVariableName().getValue();
                        if (key.equals(Constants.CONTRACT)) {
                            if (keyValue.getValue() instanceof BLangLiteral) {
                                BLangLiteral value = (BLangLiteral) keyValue.getValue();
                                if (value.getValue() instanceof String) {
                                    contractURI = (String) value.getValue();
                                } else {
                                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                            "Contract path should be applied as a string value");
                                }
                            }
                        } else if (key.equals(Constants.TAGS)) {
                            if (keyValue.getValue() instanceof BLangListConstructorExpr) {
                                BLangListConstructorExpr bLangListConstructorExpr =
                                        (BLangListConstructorExpr) keyValue.getValue();
                                for (BLangExpression bLangExpression : bLangListConstructorExpr.getExpressions()) {
                                    if (bLangExpression instanceof BLangLiteral) {
                                        BLangLiteral expression = (BLangLiteral) bLangExpression;
                                        if (expression.getValue() instanceof String) {
                                            tags.add((String) expression.getValue());
                                        } else {
                                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                                    "Tags should be applied as string values");
                                        }
                                    }
                                }
                            }
                        } else if (key.equals(Constants.OPERATIONS)) {
                            if (keyValue.getValue() instanceof BLangListConstructorExpr) {
                                BLangListConstructorExpr bLangListConstructorExpr =
                                        (BLangListConstructorExpr) keyValue.getValue();
                                for (BLangExpression bLangExpression : bLangListConstructorExpr.getExpressions()) {
                                    if (bLangExpression instanceof BLangLiteral) {
                                        BLangLiteral expression = (BLangLiteral) bLangExpression;
                                        if (expression.getValue() instanceof String) {
                                            operations.add((String) expression.getValue());
                                        } else {
                                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                                    "Operations should be applied as string values");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (contractURI != null) {
                try {
                    OpenAPI openAPI = ValidatorUtil.parseOpenAPIFile(contractURI);
                    summarizeResources(serviceNode);
                    summarizeOpenAPI(openAPI);
                    validateOpenApiAgainstResources(serviceNode, tags, operations);
                    validateResourcesAgainstOpenApi(serviceNode, tags, operations);
                } catch (OpenApiValidatorException e) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                            e.getMessage());
                }
            }
        }
    }

    @Override
    public void process(PackageNode packageNode) {
        // Collect endpoints throughout the package.
    }

    /**
     * Extract the details to be validated from the resource.
     *
     * @param serviceNode service node
     */
    private void summarizeResources(ServiceNode serviceNode) {
        // Iterate resources available in a service and extract details to be validated.
        for (FunctionNode resource : serviceNode.getResources()) {
            AnnotationAttachmentNode annotation = null;
            ResourceSummary resourceSummary = new ResourceSummary();
            resourceSummary.setResourcePosition(resource.getPosition());
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
                    for (BLangRecordLiteral.BLangRecordKeyValue keyValue : recordLiteral.getKeyValuePairs()) {
                        if (keyValue.getKey() instanceof BLangSimpleVarRef) {
                            BLangSimpleVarRef path = (BLangSimpleVarRef) keyValue.getKey();
                            String contractAttr = path.getVariableName().getValue();
                            // Extract the path and methods of the resource.
                            if (contractAttr.equals(Constants.PATH)) {
                                if (keyValue.getValue() instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) keyValue.getValue();
                                    if (value.getValue() instanceof String) {
                                        resourceSummary.setPath((String) value.getValue());
                                        resourceSummary.setPathPosition(path.getPosition());
                                    }
                                }
                            } else if (contractAttr.equals(Constants.METHODS)) {
                                if (keyValue.getValue() instanceof BLangListConstructorExpr) {
                                    BLangListConstructorExpr methodSet = (BLangListConstructorExpr) keyValue.getValue();
                                    for (BLangExpression methodExpr : methodSet.exprs) {
                                        if (methodExpr instanceof BLangLiteral) {
                                            BLangLiteral method = (BLangLiteral) methodExpr;
                                            resourceSummary.addMethod(((String) method.value)
                                                    .toLowerCase(Locale.ENGLISH));
                                            resourceSummary.setMethodsPosition(methodSet.getPosition());
                                        }
                                    }
                                }
                            } else if (contractAttr.equals(Constants.BODY)) {
                                if (keyValue.getValue() instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) keyValue.getValue();
                                    if (value.getValue() instanceof String) {
                                        resourceSummary.setPath((String) value.getValue());
                                        resourceSummary.setPathPosition(path.getPosition());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Extract and add the resource parameters
            if (resource.getParameters().size() > 0) {
                resourceSummary.setParameters(resource.getParameters());
            }

            // Add the resource summary to the resource summary list.
            this.resourceSummaryList.add(resourceSummary);
        }
    }

    private void validateResourcesAgainstOpenApi(ServiceNode serviceNode, List<String> tags,
                                                 List<String> operations) {
        boolean tagFilteringEnabled = tags.size() > 0;
        boolean operationFilteringEnabled = operations.size() > 0;

        for (ResourceSummary resourceSummary : resourceSummaryList) {
            OpenAPIPathSummary openAPIPathSummary = getOpenApiSummaryByPath(resourceSummary.getPath());
            if (openAPIPathSummary == null) {
                dLog.logDiagnostic(Diagnostic.Kind.ERROR, resourceSummary.getPathPosition(),
                        "Mismatch with OpenAPI contract. Path: " + resourceSummary.getPath());
            } else {
                List<String> unmatchedMethods = new ArrayList<>();
                if (!operationFilteringEnabled && !tagFilteringEnabled) {
                    for (String resourceMethod : resourceSummary.getMethods()) {
                        boolean noMatch = true;
                        for (String method : openAPIPathSummary.getAvailableOperations()) {
                            if (method.equals(resourceMethod)) {
                                noMatch = false;
                                break;
                            }
                        }

                        if (noMatch) {
                            unmatchedMethods.add(resourceMethod);
                        }

                        List<OpenAPIParameter> operationParamNames = openAPIPathSummary
                                .getParamNamesForOperation(resourceMethod);
                        List<ResourceParameter> resourceParamNames = resourceSummary.getParamNames();
                        Map<String, Schema> requestBodies = openAPIPathSummary.getRequestBodyForOperation(resourceMethod);
                        for (ResourceParameter parameter : resourceParamNames) {
                            boolean isExist = false;
                            for (OpenAPIParameter openAPIParameter : operationParamNames) {
                                if (parameter.getName().equals(resourceSummary.getBody())) {
                                    // TODO: Process request body.
                                    isExist = true;
                                } else if (openAPIParameter.isTypeAvailableAsRef()) {
                                    if (parameter.getName().equals(openAPIParameter.getName())) {
                                        isExist = true;
                                        Schema schema = openAPIComponentSummary
                                                .getSchema(openAPIParameter.getLocalRef());
                                        if (schema != null) {
                                            isExist = validateResourceAgainstOpenAPIParams(parameter.getParameter().symbol, schema);
                                        }
                                    }
                                } else if (openAPIParameter.getName().equals(parameter.getName())) {
                                    isExist = this.validateResourceAgainstOpenAPIParams(parameter.getParameter().symbol,
                                            openAPIParameter.getParameter().getSchema());
                                }
                            }

                            if (!isExist) {
                                dLog.logDiagnostic(Diagnostic.Kind.ERROR, parameter.getParameter().getPosition(),
                                        "Mismatch with OpenAPI contract. Couldn't " +
                                                "find documentation for the parameter "
                                                + parameter.getName() + " for the method " + resourceMethod
                                                + " of the Path: " + resourceSummary.getPath());
                            }
                        }
                    }

                    String methods = getUnmatchedMethodList(unmatchedMethods);
                    if (!openAPIPathSummary.getAvailableOperations().containsAll(resourceSummary.getMethods())) {
                        dLog.logDiagnostic(Diagnostic.Kind.ERROR, resourceSummary.getMethodsPosition(),
                                "Mismatch with OpenAPI contract. Couldn't find documentation for http method(s) "
                                        + methods + " for the Path: " + resourceSummary.getPath());
                    }
                }
            }
        }
    }

    private String getUnmatchedMethodList(List<String> unmatchedMethods) {
        StringBuilder methods = new StringBuilder();
        for (int i = 0; i < unmatchedMethods.size(); i++) {
            if (i == 0) {
                methods.append(unmatchedMethods.get(i));
            } else {
                methods.append(", ").append(unmatchedMethods.get(i));
            }
        }

        return methods.toString();
    }

    private OpenAPIPathSummary getOpenApiSummaryByPath(String path) {
        OpenAPIPathSummary openAPISummary = null;
        for (OpenAPIPathSummary openAPI : openAPISummaryList) {
            if (openAPI.getPath().equals(path)) {
                openAPISummary = openAPI;
                break;
            }
        }
        return openAPISummary;
    }

    private void validateOpenApiAgainstResources(ServiceNode serviceNode, List<String> tags,
                                                 List<String> operations) {
        boolean tagFilteringEnabled = tags.size() > 0;
        boolean operationFilteringEnabled = operations.size() > 0;

        for (OpenAPIPathSummary openApiSummary : openAPISummaryList) {
            List<ResourceSummary> resourceSummaries = getResourceSummaryByPath(openApiSummary.getPath());
            if (resourceSummaries == null) {
                dLog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                        "Mismatch with OpenAPI contract. Implementation is missing for the path: "
                                + openApiSummary.getPath());
            } else {
                List<String> allAvailableResourceMethods = getAllMethodsInResourceSummaries(resourceSummaries);
                List<String> unmatchedMethods = new ArrayList<>();

                // If operation filtering available proceed.
                // Else proceed to check tag filtering.
                if (operationFilteringEnabled) {
                    // If tag filtering available validate only the filtered operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    if (tagFilteringEnabled) {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            if (operations.contains(method) && openApiSummary.hasTags(tags, method)) {
                                validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods, method,
                                        openApiSummary, resourceSummaries, serviceNode);
                            }
                        }

                        if (unmatchedMethods.size() > 0) {
                            String methods = getUnmatchedMethodList(unmatchedMethods);
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                    "Mismatch with OpenAPI contract. " +
                                            "Implementation is missing for http method(s) " +
                                            methods + " for the path: " + openApiSummary.getPath());
                        }
                    } else {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            if (operations.contains(method)) {
                                validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods, method,
                                        openApiSummary, resourceSummaries, serviceNode);
                            }
                        }

                        if (unmatchedMethods.size() > 0) {
                            String methods = getUnmatchedMethodList(unmatchedMethods);
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                    "Mismatch with OpenAPI contract. " +
                                            "Implementation is missing for http method(s) " +
                                            methods + " for the path: " + openApiSummary.getPath());
                        }
                    }
                } else {
                    // If tag filtering available proceed to validate all the operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    if (tagFilteringEnabled) {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            if (openApiSummary.hasTags(tags, method)) {
                                validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods, method,
                                        openApiSummary, resourceSummaries, serviceNode);
                            }
                        }

                        if (unmatchedMethods.size() > 0) {
                            String methods = getUnmatchedMethodList(unmatchedMethods);
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                    "Mismatch with OpenAPI contract. " +
                                            "Implementation is missing for http method(s) " +
                                            methods + " for the path: " + openApiSummary.getPath());
                        }
                    } else {
                        for (String method : openApiSummary.getAvailableOperations()) {
                            validateOperationForOpenAPI(unmatchedMethods, allAvailableResourceMethods, method,
                                    openApiSummary, resourceSummaries, serviceNode);
                        }

                        String methods = getUnmatchedMethodList(unmatchedMethods);
                        if (!allAvailableResourceMethods.containsAll(openApiSummary.getAvailableOperations())) {
                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                    "Mismatch with OpenAPI contract. " +
                                            "Implementation is missing for http method(s) " +
                                            methods + " for the path: " + openApiSummary.getPath());
                        }
                    }
                }
            }
        }
    }

    private void validateOperationForOpenAPI(List<String> unmatchedMethods, List<String> allAvailableResourceMethods,
                                             String method, OpenAPIPathSummary openApiSummary,
                                             List<ResourceSummary> resourceSummaries, ServiceNode serviceNode) {
        boolean noMatch = true;
        for (String resourceMethod : allAvailableResourceMethods) {
            if (resourceMethod.equals(method)) {
                noMatch = false;
                break;
            }
        }

        if (noMatch) {
            unmatchedMethods.add(method);
        }

        // Check for parameter mismatch.
        checkForParameterMismatch(openApiSummary, resourceSummaries, method, serviceNode);
    }

    private void checkForParameterMismatch(OpenAPIPathSummary openApiSummary, List<ResourceSummary> resourceSummaries,
                                           String method, ServiceNode serviceNode) {
        List<OpenAPIParameter> operationParamNames = openApiSummary
                .getParamNamesForOperation(method);
        ResourceSummary resourceSummaryForMethod = getResourceSummaryByMethod(resourceSummaries, method);
        if (resourceSummaryForMethod != null) {
            List<ResourceParameter> resourceParamNames = resourceSummaryForMethod.getParamNames();
            for (OpenAPIParameter openAPIParameter : operationParamNames) {
                boolean isExist = false;
                for (ResourceParameter parameter : resourceParamNames) {
                    if (openAPIParameter.isTypeAvailableAsRef()) {
                        if (openAPIParameter.getName().equals(parameter.getName())) {
                            isExist = true;
                            Schema schema = openAPIComponentSummary.getSchema(openAPIParameter.getLocalRef());
                            if (schema != null) {
                                isExist = this.validateOpenAPIAgainResourceParams(parameter, parameter.getParameter().symbol, schema);
                            }
                        }
                    } else if (openAPIParameter.getName().equals(parameter.getName())) {
                        isExist = this.validateOpenAPIAgainResourceParams(parameter, parameter.getParameter().symbol,
                                openAPIParameter.getParameter().getSchema());
                    }
                }

                if (!isExist) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                            "Mismatch with OpenAPI contract. Implementation " +
                                    "is missing for parameter " + openAPIParameter.getName() +
                                    " for the method " + method + " of the Path: " +
                                    resourceSummaryForMethod.getPath());
                }
            }
        }
    }

    private boolean validateResourceAgainstOpenAPIParams(BVarSymbol resourceParameterType, Schema openAPIParam) {
        if (resourceParameterType.getType().getKind().typeName().equals("record")
                && openAPIParam.getType().equals("object")) {
            // Check the existence of the fields.
            Map<String, Schema> properties = ((ObjectSchema) openAPIParam).getProperties();
            BRecordType recordType = (BRecordType) resourceParameterType.getType();
            for (BField field : recordType.fields) {
                boolean isExist = false;
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    if (entry.getKey().equals(field.name.getValue())
                            && field.getType().getKind().typeName()
                            .equals(convertOpenAPITypeToBallerina(entry.getValue().getType()))) {
                        isExist = true;
                        if (convertOpenAPITypeToBallerina(entry.getValue().getType()).equals("record")) {
                            isExist = validateResourceAgainstOpenAPIParams(field.symbol, entry.getValue());
                        }
                    }
                }

                if (!isExist) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, field.pos,
                            "Mismatch with OpenAPI contract. Couldn't " +
                                    "find documentation for the field " + field.name.getValue());
                }
            }
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("[]")
                && openAPIParam.getType().equals("array")) {
            // TODO: Implement array type handling.
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("string")
                && openAPIParam.getType().equals("string")) {
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("int")
                && openAPIParam.getType().equals("int")) {
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("boolean")
                && openAPIParam.getType().equals("boolean")) {
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("decimal")
                && openAPIParam.getType().equals("number")) {
            return true;
        }
        return false;
    }

    private boolean validateOpenAPIAgainResourceParams(ResourceParameter resourceParam, BVarSymbol resourceParameterType, Schema openAPIParam) {
        if (resourceParameterType.getType().getKind().typeName().equals("record")
                && openAPIParam.getType().equals("object")) {
            // Check the existence of the fields.
            Map<String, Schema> properties = ((ObjectSchema) openAPIParam).getProperties();
            BRecordType recordType = (BRecordType) resourceParameterType.getType();
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                boolean isExist = false;
                for (BField field : recordType.fields) {
                    if (entry.getKey().equals(field.name.getValue())
                            && field.getType().getKind().typeName()
                            .equals(convertOpenAPITypeToBallerina(entry.getValue().getType()))) {
                        isExist = true;
                        if (convertOpenAPITypeToBallerina(entry.getValue().getType()).equals("record")) {
                            isExist = validateOpenAPIAgainResourceParams(resourceParam, field.symbol, entry.getValue());
                        }
                    }
                }

                if (!isExist) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, resourceParam.getParameter().getPosition(),
                            "Mismatch with OpenAPI contract. No implementation " +
                                    "found for the field " + entry.getKey());
                }
            }
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("[]")
                && openAPIParam.getType().equals("array")) {
            // TODO: Implement array type handling.
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("string")
                && openAPIParam.getType().equals("string")) {
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("int")
                && openAPIParam.getType().equals("int")) {
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("boolean")
                && openAPIParam.getType().equals("boolean")) {
            return true;
        } else if (resourceParameterType.getType().getKind().typeName().equals("decimal")
                && openAPIParam.getType().equals("number")) {
            return true;
        }
        return false;
    }

    private ResourceSummary getResourceSummaryByMethod(List<ResourceSummary> resourceSummaries, String method) {
        ResourceSummary matchingResource = null;
        for (ResourceSummary resourceSummary : resourceSummaries) {
            if (resourceSummary.isMethodAvailable(method)) {
                matchingResource = resourceSummary;
                break;
            }
        }
        return matchingResource;
    }

    private List<String> getAllMethodsInResourceSummaries(List<ResourceSummary> resourceSummaries) {
        List<String> methods = new ArrayList<>();
        for (ResourceSummary resourceSummary : resourceSummaries) {
            methods.addAll(resourceSummary.getMethods());
        }

        return methods;
    }

    private List<ResourceSummary> getResourceSummaryByPath(String path) {
        List<ResourceSummary> resourceSummaries = null;
        for (ResourceSummary resourceSummary : resourceSummaryList) {
            if (resourceSummary.getPath().equals(path)) {
                if (resourceSummaries == null) {
                    resourceSummaries = new ArrayList<>();
                    resourceSummaries.add(resourceSummary);
                } else {
                    resourceSummaries.add(resourceSummary);
                }
            }
        }
        return resourceSummaries;
    }

    private void summarizeOpenAPI(OpenAPI contract) {
        Paths paths = contract.getPaths();
        for (Map.Entry pathItem : paths.entrySet()) {
            OpenAPIPathSummary openAPISummary = new OpenAPIPathSummary();
            if (pathItem.getKey() instanceof String
                    && pathItem.getValue() instanceof PathItem) {
                String key = (String) pathItem.getKey();
                openAPISummary.setPath(key);

                PathItem operations = (PathItem) pathItem.getValue();
                if (operations.getGet() != null) {
                    openAPISummary.addAvailableOperation(Constants.GET);
                    openAPISummary.addOperation(Constants.GET, operations.getGet());
                }

                if (operations.getPost() != null) {
                    openAPISummary.addAvailableOperation(Constants.POST);
                    openAPISummary.addOperation(Constants.POST, operations.getPost());
                }

                if (operations.getPut() != null) {
                    openAPISummary.addAvailableOperation(Constants.PUT);
                    openAPISummary.addOperation(Constants.PUT, operations.getPut());
                }

                if (operations.getDelete() != null) {
                    openAPISummary.addAvailableOperation(Constants.DELETE);
                    openAPISummary.addOperation(Constants.DELETE, operations.getDelete());
                }

                if (operations.getHead() != null) {
                    openAPISummary.addAvailableOperation(Constants.HEAD);
                    openAPISummary.addOperation(Constants.HEAD, operations.getHead());
                }

                if (operations.getPatch() != null) {
                    openAPISummary.addAvailableOperation(Constants.PATCH);
                    openAPISummary.addOperation(Constants.PATCH, operations.getPatch());
                }

                if (operations.getOptions() != null) {
                    openAPISummary.addAvailableOperation(Constants.OPTIONS);
                    openAPISummary.addOperation(Constants.OPTIONS, operations.getOptions());
                }

                if (operations.getTrace() != null) {
                    openAPISummary.addAvailableOperation(Constants.TRACE);
                    openAPISummary.addOperation(Constants.TRACE, operations.getTrace());
                }
            }

            openAPISummaryList.add(openAPISummary);
        }

        this.openAPIComponentSummary.setComponents(contract.getComponents());
    }

    String convertOpenAPITypeToBallerina(String type) {
        String convertedType;
        switch (type) {
            case "integer":
                convertedType = "int";
                break;
            case "string":
                convertedType = "string";
                break;
            case "boolean":
                convertedType = "boolean";
                break;
            case "array":
                convertedType = "array";
                break;
            case "object":
                convertedType = "record";
                break;
            default:
                convertedType = "";
        }

        return convertedType;
    }
}
