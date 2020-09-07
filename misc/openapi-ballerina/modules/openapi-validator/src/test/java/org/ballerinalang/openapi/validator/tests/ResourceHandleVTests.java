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
package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResourceMethod;
import org.ballerinalang.openapi.validator.ResourceValidator;
import org.ballerinalang.openapi.validator.ResourceWithOperationId;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.error.OpenapiServiceValidationError;
import org.ballerinalang.openapi.validator.error.ResourceValidationError;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Valid test for resource validation in validateResourceAgainstOperation function.
 */
public class ResourceHandleVTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/resourceHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private BLangService extractBLangservice;
    private List<ResourceValidationError> validationErrors = new ArrayList<>();
    private List<OpenapiServiceValidationError> serviceValidationErrors = new ArrayList<>();
    private ResourceMethod resourceMethod;
    private Operation operation;
    private List<ValidationError> resourceValidationErrors = new ArrayList<>();

    @Test(enabled = false, description = "Test for checking whether resource paths " +
            "are documented in openapi contract")
    public void testResourcePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/valid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = ResourceWithOperationId.checkOperationIsAvailable(api, extractBLangservice);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Test for checking whether resource path " +
            "methods are documented in openapi contract")
    public void testResourcePathMethods() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/valid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = ResourceWithOperationId.checkOperationIsAvailable(api, extractBLangservice);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Test for checking whether openapi service paths are " +
            "documented in ballerina resource")
    public void testServicePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/servicePetstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/valid/servicePetstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        serviceValidationErrors =
                ResourceWithOperationId.checkServiceAvailable(ResourceWithOperationId.summarizeOpenAPI(api),
                        extractBLangservice);
        Assert.assertTrue(serviceValidationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Test resource function node with openapi operation ")
    public void testResourceFunctionNode() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreFunctionNode.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/valid/petstoreFunctionNode.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "get");
        operation = api.getPaths().get("/pets/{petId}").getGet();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Test resource function node with openapi operation ")
    public void testInlineRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/inline-record.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/valid/inline-record.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/user").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.isEmpty());
    }
}
