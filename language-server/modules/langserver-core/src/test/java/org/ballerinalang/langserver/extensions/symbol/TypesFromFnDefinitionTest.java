/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.symbol;

import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.IntersectionType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.diagramutil.connector.models.connector.types.UnionType;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.symbol.ResolvedTypeForSymbol;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypesFromSymbolResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

/**
 * Test type info retrieved via getTypesFromFnDefinition endpoint.
 */
public class TypesFromFnDefinitionTest {
    private Endpoint serviceEndpoint;

    private final Path typesFromFnDefinitionBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("typesFromFnDefinition.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "type info retrieved for primitive type param and primitive type return")
    public void testTypesForPrimitiveTypeParamAndPrimitiveTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(32, 9);
        LinePosition paramPosition = LinePosition.from(32, 27);
        LinePosition returnTypeDescPosition = LinePosition.from(32, 40);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.STRING);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(paramType.getType().typeName, SymbolServiceTestUtil.STRING);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for record type param and record type return")
    public void testTypesForRecordTypeParamAndRecordTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(34, 9);
        LinePosition paramPosition = LinePosition.from(34, 27);
        LinePosition returnTypeDescPosition = LinePosition.from(34, 43);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        assertStudentType(returnType.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        assertPersonType(paramType.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for multiple params and record type return")
    public void testTypesForMultipleParamsAndRecordTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(36, 9);
        LinePosition param1Position = LinePosition.from(36, 27);
        LinePosition param2Position = LinePosition.from(36, 42);
        LinePosition param3Position = LinePosition.from(36, 61);
        LinePosition returnTypeDescPosition = LinePosition.from(36, 81);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        assertStudentType(returnType.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));

        ResolvedTypeForSymbol param1Type = typesFromSymbolResponse.getTypes().get(1);
        assertPersonType(param1Type.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param1Position, param1Type.getRequestedPosition()));

        ResolvedTypeForSymbol param2Type = typesFromSymbolResponse.getTypes().get(2);
        assertCourseType(param2Type.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param2Position, param2Type.getRequestedPosition()));

        ResolvedTypeForSymbol param3Type = typesFromSymbolResponse.getTypes().get(3);
        assertSubmissionType(param3Type.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param3Position, param3Type.getRequestedPosition()));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for multiple params and primitive type return")
    public void testTypesForMultipleParamsAndPrimitiveTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(38, 9);
        LinePosition param1Position = LinePosition.from(38, 27);
        LinePosition param2Position = LinePosition.from(38, 39);
        LinePosition returnTypeDescPosition = LinePosition.from(38, 50);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.FLOAT);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));

        ResolvedTypeForSymbol param1Type = typesFromSymbolResponse.getTypes().get(1);
        assertPersonType(param1Type.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param1Position, param1Type.getRequestedPosition()));

        ResolvedTypeForSymbol param2Type = typesFromSymbolResponse.getTypes().get(2);
        Assert.assertEquals(param2Type.getType().typeName, SymbolServiceTestUtil.INTEGER);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param2Position, param2Type.getRequestedPosition()));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for record type param and record type return (types from modules)")
    public void testTypesForRecordTypeParamAndRecordTypeReturn2()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(40, 9);
        LinePosition paramPosition = LinePosition.from(40, 39);
        LinePosition returnTypeDescPosition = LinePosition.from(40, 57);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        assertTypeIdType(returnType.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        assertModuleIdType(paramType.getType());
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for primitive type array param and primitive type array return")
    public void testTypesForPrimitiveTypeArrayParamAndPrimitiveTypeArrayReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(42, 9);
        LinePosition paramPosition = LinePosition.from(42, 29);
        LinePosition returnTypeDescPosition = LinePosition.from(42, 44);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));
        Assert.assertTrue(returnType.getType() instanceof ArrayType);
        ArrayType arrayReturnType = (ArrayType) returnType.getType();
        Assert.assertEquals(arrayReturnType.memberType.typeName, SymbolServiceTestUtil.STRING);

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(paramType.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));
        Assert.assertTrue(paramType.getType() instanceof ArrayType);
        ArrayType arrayParamType = (ArrayType) paramType.getType();
        Assert.assertEquals(arrayParamType.memberType.typeName, SymbolServiceTestUtil.STRING);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for record type array params and record type array return")
    public void testTypesForRecordTypeArrayParamsAndRecordTypeArrayReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(44, 9);
        LinePosition param1Position = LinePosition.from(44, 29);
        LinePosition param2Position = LinePosition.from(44, 46);
        LinePosition returnTypeDescPosition = LinePosition.from(44, 63);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));
        Assert.assertTrue(returnType.getType() instanceof ArrayType);
        ArrayType arrayReturnType = (ArrayType) returnType.getType();
        assertStudentType(arrayReturnType.memberType);

        ResolvedTypeForSymbol param1Type = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(param1Type.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param1Position, param1Type.getRequestedPosition()));
        Assert.assertTrue(param1Type.getType() instanceof ArrayType);
        ArrayType arrayParamType1 = (ArrayType) param1Type.getType();
        assertPersonType(arrayParamType1.memberType);

        ResolvedTypeForSymbol param2Type = typesFromSymbolResponse.getTypes().get(2);
        Assert.assertEquals(param2Type.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(param2Position, param2Type.getRequestedPosition()));
        Assert.assertTrue(param2Type.getType() instanceof ArrayType);
        ArrayType arrayParamType2 = (ArrayType) param2Type.getType();
        assertCourseType(arrayParamType2.memberType);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for record array params and record array return (types from modules)")
    public void testTypesForRecordTypeArrayParamsAndRecordTypeArrayReturn2()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(46, 9);
        LinePosition paramPosition = LinePosition.from(46, 41);
        LinePosition returnTypeDescPosition = LinePosition.from(46, 60);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));
        Assert.assertTrue(returnType.getType() instanceof ArrayType);
        ArrayType arrayReturnType = (ArrayType) returnType.getType();
        assertTypeIdType(arrayReturnType.memberType);

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(paramType.getType().typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));
        Assert.assertTrue(paramType.getType() instanceof ArrayType);
        ArrayType arrayParamType = (ArrayType) paramType.getType();
        assertModuleIdType(arrayParamType.memberType);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for optional record type param and optional record type return")
    public void testTypesForOptionalRecordTypeParamAndOptionalRecordTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(48, 9);
        LinePosition paramPosition = LinePosition.from(48, 28);
        LinePosition returnTypeDescPosition = LinePosition.from(48, 44);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.UNION);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));
        Assert.assertTrue(returnType.getType() instanceof UnionType);
        UnionType unionReturnType = (UnionType) returnType.getType();
        Assert.assertEquals(unionReturnType.members.size(), 2);
        assertStudentType(unionReturnType.members.get(0));
        Assert.assertEquals(unionReturnType.members.get(1).typeName, SymbolServiceTestUtil.NULL);

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.UNION);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));
        Assert.assertTrue(paramType.getType() instanceof UnionType);
        UnionType unionParamType = (UnionType) paramType.getType();
        Assert.assertEquals(unionParamType.members.size(), 2);
        assertPersonType(unionParamType.members.get(0));
        Assert.assertEquals(unionParamType.members.get(1).typeName, SymbolServiceTestUtil.NULL);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for primitive type defaultable param and primitive type return")
    public void testTypesForPrimitiveTypeDefaultableParamAndPrimitiveTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(50, 9);
        LinePosition paramPosition = LinePosition.from(50, 28);
        LinePosition returnTypeDescPosition = LinePosition.from(50, 46);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.UNION);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));
        Assert.assertTrue(returnType.getType() instanceof UnionType);
        UnionType unionReturnType = (UnionType) returnType.getType();
        Assert.assertEquals(unionReturnType.members.size(), 2);
        Assert.assertEquals(unionReturnType.members.get(0).typeName, SymbolServiceTestUtil.STRING);
        Assert.assertEquals(unionReturnType.members.get(1).typeName, SymbolServiceTestUtil.ERROR);

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(paramType.getType().typeName, SymbolServiceTestUtil.STRING);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));
        Assert.assertTrue(paramType.getType().defaultable);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for inline record type param and inline record type return")
    public void testTypesForInlineRecordTypeParamAndInlineRecordTypeReturn()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(52, 9);
        LinePosition paramPosition = LinePosition.from(52, 74);
        LinePosition returnTypeDescPosition = LinePosition.from(53, 12);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(
                returnTypeDescPosition, returnType.getRequestedPosition()));
        Assert.assertNull(returnType.getType().name);
        Assert.assertTrue(returnType.getType() instanceof RecordType);
        RecordType inlineRecordType1 = (RecordType) returnType.getType();
        Assert.assertEquals(inlineRecordType1.fields.size(), 4);
        Assert.assertEquals(inlineRecordType1.fields.get(3).typeName, SymbolServiceTestUtil.INTEGER);
        Assert.assertEquals(inlineRecordType1.fields.get(3).name, "zip");

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(paramType.getType().typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));
        Assert.assertNull(paramType.getType().name);
        Assert.assertTrue(paramType.getType() instanceof RecordType);
        RecordType inlineRecordType2 = (RecordType) paramType.getType();
        Assert.assertEquals(inlineRecordType2.fields.size(), 3);
        Assert.assertEquals(inlineRecordType2.fields.get(2).typeName, SymbolServiceTestUtil.STRING);
        Assert.assertEquals(inlineRecordType2.fields.get(2).name, "city");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test no params and no return type")
    public void testNoParamsAndNoReturnType()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(55, 9);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, null, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), 1);
        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.NULL);
        Assert.assertNull(returnType.getType().name);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test primitive type param and no return type")
    public void testPrimitiveTypeParamAndNoReturnType()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(57, 9);
        LinePosition paramPosition = LinePosition.from(57, 28);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, null, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.NULL);
        Assert.assertNull(returnType.getType().name);

        ResolvedTypeForSymbol paramType = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertEquals(paramType.getType().typeName, SymbolServiceTestUtil.STRING);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(paramPosition, paramType.getRequestedPosition()));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test no params and primitive return type")
    public void testNoParamsAndPrimitiveReturnType()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(59, 9);
        LinePosition returnTypeDescPosition = LinePosition.from(59, 31);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), 1);
        ResolvedTypeForSymbol returnType = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.STRING);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test invalid file path")
    public void testInvalidFilePath() throws ExecutionException, InterruptedException {
        LinePosition fnPosition = LinePosition.from(59, 9);
        LinePosition returnTypeDescPosition = LinePosition.from(59, 31);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                URI.create("file://+"), fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), 0);
    }

    @Test(description = "test empty positions")
    public void testEmptyPositions() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnDefinitionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, null, null, this.serviceEndpoint);

        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), 0);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    private void assertPersonType(Type resolvedType) {
        Assert.assertEquals(resolvedType.typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertEquals(resolvedType.name, "Person");
        Assert.assertTrue(resolvedType instanceof RecordType);
        RecordType personRecordType = (RecordType) resolvedType;
        Assert.assertEquals(personRecordType.fields.size(), 4);

        Assert.assertEquals(personRecordType.fields.get(0).name, "id");
        Assert.assertEquals(personRecordType.fields.get(0).typeName, SymbolServiceTestUtil.INTEGER);

        Assert.assertEquals(personRecordType.fields.get(1).name, "firstName");
        Assert.assertEquals(personRecordType.fields.get(1).typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(personRecordType.fields.get(2).name, "lastName");
        Assert.assertEquals(personRecordType.fields.get(2).typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(personRecordType.fields.get(3).name, "age");
        Assert.assertEquals(personRecordType.fields.get(3).typeName, SymbolServiceTestUtil.INTEGER);
        Assert.assertTrue(personRecordType.fields.get(3).optional);
    }

    private void assertCourseType(Type resolvedType) {
        Assert.assertEquals(resolvedType.typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertEquals(resolvedType.name, "Course");
        Assert.assertTrue(resolvedType instanceof RecordType);
        RecordType courseRecordType = (RecordType) resolvedType;
        Assert.assertEquals(courseRecordType.fields.size(), 3);

        Assert.assertEquals(courseRecordType.fields.get(0).name, "id");
        Assert.assertEquals(courseRecordType.fields.get(0).typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(courseRecordType.fields.get(1).name, "name");
        Assert.assertEquals(courseRecordType.fields.get(1).typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(courseRecordType.fields.get(2).name, "credits");
        Assert.assertEquals(courseRecordType.fields.get(2).typeName, SymbolServiceTestUtil.INTEGER);
    }

    private void assertSubmissionType(Type resolvedType) {
        Assert.assertEquals(resolvedType.typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertEquals(resolvedType.name, "Submission");
        Assert.assertTrue(resolvedType instanceof RecordType);
        RecordType submissionRecordType = (RecordType) resolvedType;
        Assert.assertEquals(submissionRecordType.fields.size(), 3);

        Assert.assertEquals(submissionRecordType.fields.get(0).name, "id");
        Assert.assertEquals(submissionRecordType.fields.get(0).typeName, SymbolServiceTestUtil.INTEGER);
        Assert.assertTrue(submissionRecordType.fields.get(0).defaultable);

        Assert.assertEquals(submissionRecordType.fields.get(1).name, "date");
        Assert.assertEquals(submissionRecordType.fields.get(1).typeName, SymbolServiceTestUtil.STRING);

        assertModuleIdType(submissionRecordType.fields.get(2));
    }

    private void assertStudentType(Type resolvedType) {
        Assert.assertEquals(resolvedType.typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertEquals(resolvedType.name, "Student");
        Assert.assertTrue(resolvedType instanceof RecordType);
        RecordType studentRecordType = (RecordType) resolvedType;
        Assert.assertEquals(studentRecordType.fields.size(), 5);

        Assert.assertEquals(studentRecordType.fields.get(0).typeName, SymbolServiceTestUtil.UNION);
        Assert.assertEquals(studentRecordType.fields.get(0).name, "id");
        Assert.assertTrue(studentRecordType.fields.get(0) instanceof UnionType);
        UnionType unionType = (UnionType) studentRecordType.fields.get(0);
        Assert.assertEquals(unionType.members.size(), 2);
        Assert.assertEquals(unionType.members.get(0).typeName, SymbolServiceTestUtil.INTEGER);
        Assert.assertEquals(unionType.members.get(1).typeName, SymbolServiceTestUtil.NULL);

        Assert.assertEquals(studentRecordType.fields.get(1).name, "fullName");
        Assert.assertEquals(studentRecordType.fields.get(1).typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(studentRecordType.fields.get(2).name, "age");
        Assert.assertEquals(studentRecordType.fields.get(2).typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(studentRecordType.fields.get(3).name, "courses");
        Assert.assertTrue(studentRecordType.fields.get(3) instanceof ArrayType);
        ArrayType coursesArrayType = (ArrayType) studentRecordType.fields.get(3);
        Assert.assertTrue(coursesArrayType.memberType instanceof RecordType);
        RecordType courseRecordType = (RecordType) coursesArrayType.memberType;
        Assert.assertEquals(courseRecordType.fields.size(), 2);
        Assert.assertEquals(courseRecordType.fields.get(0).name, "title");
        Assert.assertEquals(courseRecordType.fields.get(0).typeName, SymbolServiceTestUtil.STRING);
        Assert.assertEquals(courseRecordType.fields.get(1).name, "credits");
        Assert.assertEquals(courseRecordType.fields.get(1).typeName, SymbolServiceTestUtil.INTEGER);

        Assert.assertEquals(studentRecordType.fields.get(4).name, "totalCredits");
        Assert.assertEquals(studentRecordType.fields.get(4).typeName, SymbolServiceTestUtil.INTEGER);
    }

    private void assertModuleIdType(Type resolvedType) {
        Assert.assertEquals(resolvedType.typeName, SymbolServiceTestUtil.INTERSECTION);
        Assert.assertEquals(resolvedType.typeInfo.name, "ModuleId");
        Assert.assertEquals(resolvedType.typeInfo.orgName, "ballerina");
        Assert.assertEquals(resolvedType.typeInfo.moduleName, "lang.typedesc");
        Assert.assertTrue(resolvedType instanceof IntersectionType);
        IntersectionType moduleIdRecordType = (IntersectionType) resolvedType;
        Assert.assertEquals(moduleIdRecordType.members.size(), 2);

        Assert.assertEquals(moduleIdRecordType.members.get(0).typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertNull(moduleIdRecordType.members.get(0).name);
        Assert.assertTrue(moduleIdRecordType.members.get(0) instanceof RecordType);
        RecordType recordType = (RecordType) moduleIdRecordType.members.get(0);

        Assert.assertEquals(recordType.fields.size(), 3);
        Assert.assertEquals(recordType.fields.get(0).name, "organization");
        Assert.assertEquals(recordType.fields.get(0).typeName, SymbolServiceTestUtil.STRING);
        Assert.assertEquals(recordType.fields.get(1).name, "name");
        Assert.assertEquals(recordType.fields.get(1).typeName, SymbolServiceTestUtil.STRING);
        Assert.assertEquals(recordType.fields.get(2).name, "platformParts");
        Assert.assertEquals(recordType.fields.get(2).typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(recordType.fields.get(2) instanceof ArrayType);
        ArrayType arrayReturnType = (ArrayType) recordType.fields.get(2);
        Assert.assertEquals(arrayReturnType.memberType.typeName, SymbolServiceTestUtil.STRING);

        Assert.assertEquals(moduleIdRecordType.members.get(1).typeName, SymbolServiceTestUtil.READ_ONLY);
        Assert.assertNull(moduleIdRecordType.members.get(1).name);
    }

    private void assertTypeIdType(Type resolvedType) {
        Assert.assertEquals(resolvedType.typeName, SymbolServiceTestUtil.INTERSECTION);
        Assert.assertEquals(resolvedType.typeInfo.name, "TypeId");
        Assert.assertEquals(resolvedType.typeInfo.orgName, "ballerina");
        Assert.assertEquals(resolvedType.typeInfo.moduleName, "lang.typedesc");
        Assert.assertTrue(resolvedType instanceof IntersectionType);
        IntersectionType typeIdRecordType = (IntersectionType) resolvedType;
        Assert.assertEquals(typeIdRecordType.members.size(), 2);

        Assert.assertEquals(typeIdRecordType.members.get(0).typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertNull(typeIdRecordType.members.get(0).name);
        Assert.assertTrue(typeIdRecordType.members.get(0) instanceof RecordType);
        RecordType recordType = (RecordType) typeIdRecordType.members.get(0);

        Assert.assertEquals(recordType.fields.size(), 2);
        Assert.assertEquals(recordType.fields.get(0).name, "moduleId");
        Assert.assertEquals(recordType.fields.get(0).typeName, SymbolServiceTestUtil.INTERSECTION);
        assertModuleIdType(recordType.fields.get(0));
        Assert.assertEquals(recordType.fields.get(1).name, "localId");
        Assert.assertEquals(recordType.fields.get(1).typeName, SymbolServiceTestUtil.UNION);

        Assert.assertTrue(recordType.fields.get(1) instanceof UnionType);
        UnionType unionType = (UnionType) recordType.fields.get(1);
        Assert.assertEquals(unionType.members.size(), 2);
        Assert.assertEquals(unionType.members.get(0).typeName, SymbolServiceTestUtil.STRING);
        Assert.assertEquals(unionType.members.get(1).typeName, SymbolServiceTestUtil.INTEGER);

        Assert.assertEquals(typeIdRecordType.members.get(1).typeName, SymbolServiceTestUtil.READ_ONLY);
        Assert.assertNull(typeIdRecordType.members.get(1).name);
    }
}
