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
import org.ballerinalang.openapi.validator.ServiceValidator;
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
 * Test for operations validate with model.
 */
public class OperationHandleVTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/operationHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private BLangService extractBLangservice;
    private List<ValidationError> validationErrors = new ArrayList<>();
    private ResourceMethod resourceMethod;
    private Operation operation;

    @Test(enabled = false, description = "Operation model has path parameters primitive type")
    public void testPrimitiveTypePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstorePathPrimitive.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("operationHandle/ballerina/valid/petstorePathPrimitive.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "get");
        operation = api.getPaths().get("/pets/{petId}").getGet();
        validationErrors = ResourceValidator.validateOperationAgainstResource(operation, resourceMethod);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Operation model has path parameters object type")
    public void testObjectTypePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstorePathObject.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("operationHandle/ballerina/valid/petstorePathObject.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "get");
        operation = api.getPaths().get("/pets/{petId}").getGet();
        validationErrors = ResourceValidator.validateOperationAgainstResource(operation, resourceMethod);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Operation model has path parameters array type")
    public void testArrayTypePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstorePathArray.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("operationHandle/ballerina/valid/petstorePathArray.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "get");
        operation = api.getPaths().get("/pets/{petId}").getGet();
        validationErrors = ResourceValidator.validateOperationAgainstResource(operation, resourceMethod);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Operation model has request parameters ")
    public void testrequestBody() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreRBParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("operationHandle/ballerina/valid/petstoreRBParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        validationErrors = ResourceValidator.validateOperationAgainstResource(operation, resourceMethod);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Operation model has nestedArray type parameters ")
    public void testNestedArrayType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreNestedArrayType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("operationHandle/ballerina/valid/petstoreNestedArrayType.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/user").getPost();
        validationErrors = ResourceValidator.validateOperationAgainstResource(operation, resourceMethod);
        Assert.assertTrue(validationErrors.isEmpty());
    }
}
