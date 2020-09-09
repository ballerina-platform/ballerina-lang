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
import org.ballerinalang.openapi.validator.Constants;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.error.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for BTypeToJsonSchema Invalid tests.
 */
public class InvalidValidatorUtilTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/recordValidation" +
            "/swagger/").toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BVarSymbol extractBVarSymbol;
    private List<ValidationError> validationErrors = new ArrayList<>();

    @Test(enabled = false, description = "Test missing field in Json schema")
    public void testMissingFieldInJsonSchema() throws UnsupportedEncodingException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("invalidTests/missingFieldInJsonSchema.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/missingFieldInJsonSchema.bal");
        extractSchema = ValidatorTest.getComponet(api, "MissingFieldInJsonSchema");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.get(0)instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "phone2");
    }

    @Test(enabled = false, description = "Test missing field in ballerinaType")
    public void testMissingFieldInBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/missingFieldInBallerinaType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/missingFieldInBallerinaType.bal");
        extractSchema = ValidatorTest.getComponet(api, "MissingFieldInBallerinaType");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.get(1) instanceof MissingFieldInBallerinaType);
        Assert.assertEquals(validationErrors.get(1).getFieldName(), "middleName");
    }

    @Test(enabled = false, description = "Test type mismatch")
    public void testTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatch.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/typeMisMatch.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatch");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(enabled = false, description = "Test type mismatch with array. " +
            "Same field name has ballerina type as array and json type as string")
    public void testTypeMismatchArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatchArray.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/typeMisMatchArray.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatchArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "phone");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.ARRAY);
    }

    @Test(enabled = false, description = "Test type mismatch with array. Same field name has " +
            "ballerina type as string array and json type as integer array")
    public void testTypeMismatchArrayType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatchArrayType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/typeMisMatchArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatchArrayType");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "phone");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(enabled = false, description = "test Nested array type")
    public void testTypeMisMatchNestedArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatchNestedArrayType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/typeMisMatchNestedArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatchNestedArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "phone");
    }

    @Test(enabled = false, description = "Test record field with array type of another record")
    public void testRecordArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/recordTypeArray.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/recordTypeArray.bal");
        extractSchema = ValidatorTest.getComponet(api, "RecordTypeArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(enabled = false, description = "Test for nested record")
    public void testNestedRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/nestedRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/nestedRecord.bal");
        extractSchema = ValidatorTest.getComponet(api, "NestedRecord");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(enabled = false, description = "Test for nested 4 record")
    public void testNested4Record() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/nested4Record.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/nested4Record.bal");
        extractSchema = ValidatorTest.getComponet(api, "FourNestedComponent");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "month");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(enabled = false, description = "Test oneOf type with primitive data type")
    public void testOneOfTypewithPrimitiveData() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/primitive/oneOfPrimitive.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/primitive/oneOf.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().
                        getContent().get("application/json").getSchema();
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(((TypeMismatch) validationErrors.get(0)).getTypeBallerinaType(), Constants.Type.DECIMAL);
    }

    @Test(enabled = false, description = "Test oneOf with record type Type mismatching")
    public void testOneOfType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOf.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("recordValidation/ballerina/invalidTests/oneOf/oneOf.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent()
                        .get("application/json").getSchema();
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.get(0) instanceof OneOfTypeValidation);
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getFieldName(), "Cat");
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getBlockErrors().
                get(0).getFieldName(), "id");
    }

    @Test(enabled = false, description = "Test oneOf with record type Type mismatching")
    public void testOneOfTypeMistMatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOfTypeMismatch.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/oneOf/oneOfTypeMismatch.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().
                        get("application/json").getSchema();
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.stream().allMatch(item -> item instanceof OneOfTypeValidation));
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getFieldName(), "Dog");
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(),
                "bark");
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(1)).getFieldName(), "Cat");
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(1)).getBlockErrors().get(0).getFieldName(),
                "id");
    }

    @Test(enabled = false, description = "Test for missing fields in json schema when oneOf type record scenarios ")
    public void testOneOfMisJson() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOfMisFieldsJson.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/oneOf/oneOfMisFieldsJson.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().
                        get("application/json").getSchema();

        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.get(0) instanceof OneOfTypeValidation);
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(),
                "place");
    }

    @Test(enabled = false, description = "Test for missing fields in ballerina when oneOf type has")
    public void testOneOfMisBal() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOfBalRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "recordValidation/ballerina/invalidTests/oneOf/oneOfBalRecord.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().
                        get("application/json").getSchema();
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BTypeToJsonValidatorUtil.validate(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.stream().allMatch(item -> item instanceof OneOfTypeValidation));
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(),
                "age");
    }
}
