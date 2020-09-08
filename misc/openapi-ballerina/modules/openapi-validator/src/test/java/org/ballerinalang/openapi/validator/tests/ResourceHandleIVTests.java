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
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResourceMethod;
import org.ballerinalang.openapi.validator.ResourceValidator;
import org.ballerinalang.openapi.validator.ResourceWithOperationId;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.OpenapiServiceValidationError;
import org.ballerinalang.openapi.validator.error.ResourceValidationError;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
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
 * Test for resource validate with operation model in ResourceWithOperationId function checkOperationIsAvailable
 * and resource Validator validateResourceAgainstOperation function.
 */
public class ResourceHandleIVTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/resourceHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BLangService extractBLangservice;
    private List<ResourceValidationError> validationErrors = new ArrayList<>();
    private List<OpenapiServiceValidationError> serviceValidationErrors = new ArrayList<>();
    private ResourceMethod resourceMethod;
    private Operation operation;
    private List<ValidationError> resourceValidationErrors = new ArrayList<>();

    @Test(enabled = false, description = "Test for checking whether resource paths are documented in openapi contract")
    public void testResourcePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = ResourceWithOperationId.checkOperationIsAvailable(api, extractBLangservice);
        Assert.assertTrue(validationErrors.get(0) instanceof ResourceValidationError);
        Assert.assertEquals(validationErrors.get(0).getResourcePath(), "/extraPathPet");
    }

    @Test(enabled = false, description = "Test for checking whether " +
            "resource paths method are documented in openapi contract")
    public void testResourceExtraMethod() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreExtraMethod.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreExtraMethod.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = ResourceWithOperationId.checkOperationIsAvailable(api, extractBLangservice);
        Assert.assertTrue(validationErrors.get(0) instanceof ResourceValidationError);
        Assert.assertEquals(validationErrors.get(0).getresourceMethod(), "post");
    }

    @Test(enabled = false, description = "Test for checking whether " +
            "openapi service operations are documented in ballerina resource")
    public void testExtraServicePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreExtraServiceOperation.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "resourceHandle/ballerina/invalid/petstoreExtraServiceOperation.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        serviceValidationErrors =
                ResourceWithOperationId.checkServiceAvailable(ResourceWithOperationId.summarizeOpenAPI(api),
                        extractBLangservice);
        Assert.assertTrue(serviceValidationErrors.get(0) instanceof OpenapiServiceValidationError);
        Assert.assertEquals(serviceValidationErrors.get(0).getServiceOperation(), "post");
        Assert.assertEquals(serviceValidationErrors.get(0).getServicePath(), "/pets/{petId}");
    }

    @Test(enabled = false, description = "Test resource function node with openapi operation ")
    public void testResourceFunctionNode() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreFunctionNode.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreFunctionNode.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "get");
        operation = api.getPaths().get("/pets/{petId}").getGet();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "petId");
    }

    @Test(enabled = false, description = "Test resource function node record type parameter with openapi operation ")
    public void testResourceRecordParameter() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreRecordParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "name");
    }

    @Test(enabled = false, description = "Test resource function node record type parameter with openapi operation ")
    public void testResourceRecordParameter01() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordParameter01.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreRecordParameter01.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "place");
    }

    @Test(enabled = false, description = "Test resource function node record type parameter with openapi operation ")
    public void testResourceWithRequestBody() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRBParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreRBParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "name");
    }

    @Test(enabled = false, description = "Test resource function node record type parameter with openapi operation ")
    public void testResourceWithRBPrimitive() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRBPrimitiveParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "resourceHandle/ballerina/invalid/petstoreRBPrimitiveParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "body");
    }

    @Test(enabled = false, description = "Test resource function node oneOf type request body with openapi operation ")
    public void testResourceWithRBOneOf() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreOneOfTypeMismatch.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "resourceHandle/ballerina/invalid/petstoreOneOfTypeMismatch.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.get(0) instanceof OneOfTypeValidation);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "Dog");
        Assert.assertEquals(((OneOfTypeValidation) resourceValidationErrors.get(0))
                .getBlockErrors().get(0).getFieldName(), "bark");
    }

// oneOF path paramters not support for this
//    @Test(description = "Test resource function node oneOf type request body with openapi operation ")
//    public void testOneOfWithPath() throws OpenApiValidatorException, UnsupportedEncodingException {
//        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreOneOfPath.yaml");
//        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
//        bLangPackage = ValidatorTest.getBlangPackage(
//                "resourceHandle/ballerina/invalid/petstoreOneOfPath.bal");
//        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
//        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
//        operation = api.getPaths().get("/pets").getPost();
//        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
//        Assert.assertTrue(resourceValidationErrors.get(0) instanceof OneOfTypeValidation);
//        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "Dog");
//        Assert.assertEquals(((OneOfTypeValidation) resourceValidationErrors.get(0))
//                .getBlockErrors().get(0).getFieldName(), "bark");
//
//    }

    @Test(enabled = false, description = "Test resource function node with request body and path parameter")
    public void testRequestBodywithPathParamter() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRBwithPathParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "resourceHandle/ballerina/invalid/petstoreRBwithPathParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        resourceValidationErrors = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod);
        Assert.assertTrue(resourceValidationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(resourceValidationErrors.get(0).getFieldName(), "name");
    }
}
