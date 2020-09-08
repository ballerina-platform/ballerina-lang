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
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.BTypeToJsonValidatorUtil;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for BJsonSchemaUtil.
 */
public class ValidValidatorUtilTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/recordValidation" +
            "/swagger/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BVarSymbol extractBVarSymbol;

    @Test(enabled = false, description = "Valid test case for all test case")
    public void  testValidCase() throws OpenApiValidatorException, UnsupportedEncodingException {
//        Load yaml file
        Path contractPath = RES_DIR.resolve("validTests/valid.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
//        Load BlangPackage for given .bal file
        bLangPackage = ValidatorTest.getBlangPackage("recordValidation/ballerina/validTests/validSchema.bal");
        Schema extractSchema = ValidatorTest.getComponet(api, "Valid");
        BVarSymbol extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        Assert.assertTrue((BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(enabled = false, description = "Test type mismatch with array. " +
            "Same field name has ballerina type as string array and json " +
            "type as integer array")
    public void testTypeMismatchArrayType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/validTypeMisMatchArrayType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/validTests/validTypeMisMatchArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "ValidTypeMisMatchArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        Assert.assertTrue((BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(enabled = false, description = "Test Nested array type")
    public void testTypeMisMatchNestedArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/validTypeMisMatchNestedArrayType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/validTests/validTypeMisMatchNestedArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "ValidTypeMisMatchNestedArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        Assert.assertTrue((BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(enabled = false, description = "Test record field with array type of another record")
    public void testRecordArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/recordTypeArray.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("recordValidation/ballerina/validTests/recordTypeArray.bal");
        extractSchema = ValidatorTest.getComponet(api, "RecordTypeArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        Assert.assertTrue((BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(enabled = false, description = "Test oneOf with record type")
    public void testOneOfType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/oneOf.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("recordValidation/ballerina/validTests/oneOf.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().
                        get("application/json").getSchema();
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        List<ValidationError> validationErrors =
                BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.isEmpty());
    }

    @Test(enabled = false, description = "Test for nested record")
    public void testNestedRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/nestedRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("recordValidation/ballerina/validTests/nestedRecord.bal");
        extractSchema = ValidatorTest.getComponet(api, "NestedRecord");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        Assert.assertTrue((BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol)).isEmpty());
    }
}
