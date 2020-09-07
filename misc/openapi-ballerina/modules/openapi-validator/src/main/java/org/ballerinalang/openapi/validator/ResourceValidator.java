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

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.ballerinalang.openapi.validator.error.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This util is checking the availability of services and the operations in contract and ballerina file.
 */
public class ResourceValidator {

    /**
     * Validate the missing fields in openapi operation according to resource service.
     * @param operation         openAPi operation
     * @param resourceMethod    validate resourceMethods
     * @return                  list of validationErrors
     * @throws OpenApiValidatorException throws openApi validator exception
     */
    public static List<ValidationError> validateResourceAgainstOperation(Operation operation,
                                                                         ResourceMethod resourceMethod)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrors = new ArrayList<>();
        if (!resourceMethod.getParamNames().isEmpty()) {
            for (ResourceParameter resourceParameter: resourceMethod.getParamNames()) {
                Boolean isParameterExit = false;
                //  Handle the requestBody parameter
                if ((resourceMethod.getBody() != null) && (resourceMethod.getBody().equals(resourceParameter.getName()))
                        && (operation.getRequestBody() != null)) {
                        RequestBody requestBody = operation.getRequestBody();
                    isParameterExit =
                            validateRequestBodyResourceToOpenApi(operation, validationErrors, resourceParameter,
                                    isParameterExit, requestBody);

                    //  Handle Path parameter
                } else if (operation.getParameters() != null) {
                    for (Parameter parameter : operation.getParameters()) {
                        if (resourceParameter.getName().equals(parameter.getName()) &&
                                (parameter.getSchema() != null)) {
                            isParameterExit = true;
                            List<ValidationError> validationErrorsResource =
                                    BTypeToJsonValidatorUtil.validate(parameter.getSchema(),
                                            resourceParameter.getParameter().symbol);
                            if (!validationErrorsResource.isEmpty()) {
                                validationErrors.addAll(validationErrorsResource);
                            }
                            break;
                        }
                    }
                }
                if (!isParameterExit) {
                    ValidationError validationError = new ValidationError(resourceParameter.getName(),
                            BTypeToJsonValidatorUtil.convertTypeToEnum(resourceParameter.getType()));
                    validationErrors.add(validationError);
                }
            }
        }
        return validationErrors;
    }

    /**
     * Validate request Body parameters in resource against to openAPI.
     * @param operation             validate openApi operation
     * @param validationErrors      list of validationErrors
     * @param resourceParameter     validate resource parameter
     * @param isParameterExit       boolean tag for checking parameter is available
     * @param requestBody           requestBody type  operation parameter
     * @return  boolean value for indicating parameter is available
     * @throws OpenApiValidatorException throws openApi validator exception
     */
    private static Boolean validateRequestBodyResourceToOpenApi(Operation operation,
                                                                List<ValidationError> validationErrors,
                                                                ResourceParameter resourceParameter,
                                                                Boolean isParameterExit, RequestBody requestBody)
            throws OpenApiValidatorException {
        if ((requestBody.getContent() != null) && !getOperationRequestBody(operation).isEmpty()) {
            Map<String, Schema> requestBodySchemas = getOperationRequestBody(operation);
                for (Map.Entry<String, Schema> requestBodyOperation: requestBodySchemas.entrySet()) {
                    List<ValidationError> requestBValidationError  =
                            BTypeToJsonValidatorUtil.validate(requestBodyOperation.getValue(),
                                    resourceParameter.getParameter().symbol);
                    if (!requestBValidationError.isEmpty()) {
                        for (ValidationError validationError : requestBValidationError) {
                            if ((validationError instanceof TypeMismatch) ||
                                    (validationError instanceof MissingFieldInJsonSchema) ||
                                    (validationError instanceof OneOfTypeValidation)) {
                                validationErrors.add(validationError);
                            }
                        }
                    }
                    isParameterExit = true;
                    break;
                }
        }
        return isParameterExit;
    }

    /**
     * Get the requestBody parameter from operation.
     * @param operation     openApi operation object
     * @return Map with parameters
     */
    public static Map<String, Schema> getOperationRequestBody(Operation operation) {
        Map<String, Schema> requestBodySchemas = new HashMap<>();
        Content content = operation.getRequestBody().getContent();
        for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
            requestBodySchemas.put(mediaTypeEntry.getKey(), mediaTypeEntry.getValue().getSchema());
        }
        return requestBodySchemas;
    }

    /**
     * Validate the missing fields in resource parameter according to given operation.
     * @param operation         openApi operation
     * @param resourceMethod    resource method object
     * @return                  list of ValidationErrors
     * @throws OpenApiValidatorException throws openApi validator exception
     */
    public static List<ValidationError> validateOperationAgainstResource(Operation operation,
                                                                         ResourceMethod resourceMethod)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrorList = new ArrayList<>();
        // Handle path , query parameters
        if (operation.getParameters() != null) {
            List<Parameter> operationParam = operation.getParameters();
            for (Parameter param : operationParam) {
                Boolean isOParamExit = false;
                // Temporary solution for skipping the query parameter, this if condition can be removed when openApi
                // tool available with query parameter
                if (param instanceof QueryParameter) {
                    isOParamExit = true;
                }
                if (!resourceMethod.getParamNames().isEmpty()) {
                    for (ResourceParameter resourceParam: resourceMethod.getParamNames()) {
                        //  Check whether it is path parameter
                        if ((param instanceof PathParameter) && (param.getName().equals(resourceParam.getName()))) {
                            isOParamExit = true;
                            List<ValidationError> validationErrors =
                                    BTypeToJsonValidatorUtil.validate(param.getSchema(),
                                            resourceParam.getParameter().symbol);
                            if (!validationErrors.isEmpty()) {
                                validationErrorList.addAll(validationErrors);
                            }
                            break;
                        }
                    }
                }
                if (!isOParamExit) {
                    ValidationError validationError = new ValidationError(param.getName(),
                            BTypeToJsonValidatorUtil.convertTypeToEnum(param.getSchema().getType()));
                    validationErrorList.add(validationError);
                }
            }
        }
        //  Handle the requestBody
        if (operation.getRequestBody() != null) {
            List<ResourceParameter> resourceParam = resourceMethod.getParamNames();
            Map<String, Schema> requestBodySchemas = ResourceValidator.getOperationRequestBody(operation);
            for (Map.Entry<String, Schema> operationRB: requestBodySchemas.entrySet()) {
                Boolean isOParamExit = false;
                isOParamExit = validateRequestBodyOpenApiToResource(resourceMethod, validationErrorList, resourceParam,
                        operationRB, isOParamExit);
                if (!isOParamExit) {
                    String type = "";
                    if (operationRB.getValue().getType() == null && (operationRB.getValue().getProperties() != null)) {
                        type = "object";
                    } else {
                        type = operationRB.getValue().getType();
                    }
                    ValidationError validationError = new ValidationError(operationRB.getKey(),
                            BTypeToJsonValidatorUtil.convertTypeToEnum(type));
                    validationErrorList.add(validationError);
                }
            }
        }
        return validationErrorList;
    }

    /**
     * Validate the requestBody parameter openApi against resource.
     * @param resourceMethod            validate resource Methods
     * @param validationErrorList       list of ValidationErros
     * @param resourceParam             validate resource parameter
     * @param operationRB               request Body parameters in operations
     * @param isOParamExit              boolean tag for checking the operation parameter available
     * @return boolean value whether it is exit or not
     * @throws OpenApiValidatorException throws openApi validator exception
     */
    private static Boolean validateRequestBodyOpenApiToResource(ResourceMethod resourceMethod,
                                                                List<ValidationError> validationErrorList,
                                                                List<ResourceParameter> resourceParam,
                                                                Map.Entry<String, Schema> operationRB,
                                                                Boolean isOParamExit) throws OpenApiValidatorException {

        if (!resourceParam.isEmpty()) {
            for (ResourceParameter resourceParameter : resourceParam) {
                if (resourceMethod.getBody().equals(resourceParameter.getName())) {
                    List<ValidationError> validationErrors =
                            BTypeToJsonValidatorUtil.validate(operationRB.getValue(),
                                    resourceParameter.getParameter().symbol);
                    if (validationErrors.isEmpty()) {
                        isOParamExit = true;
                    } else {
                        for (ValidationError validEr: validationErrors) {
                            if ((validEr instanceof MissingFieldInBallerinaType) ||
                                    (validEr instanceof OneOfTypeValidation) ||
                                    (validEr instanceof TypeMismatch)) {
                                validationErrorList.add(validEr);
                                isOParamExit = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return isOParamExit;
    }
}
