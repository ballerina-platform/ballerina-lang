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

import io.ballerina.tools.text.LineRange;
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.symbol.ResolvedTypeForExpression;
import org.ballerinalang.langserver.extensions.ballerina.symbol.TypesFromExpressionResponse;
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
 * Test type info retrieved via getTypeFromExpression endpoint.
 */
public class TypeFromExpressionTest {
    private Endpoint serviceEndpoint;

    private final Path typeFromExpressionBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("typeFromExpression.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "type info retrieved for a conditional expression node")
    public void testTypeForConditionalExprNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromExpressionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LineRange[] ranges = {SymbolServiceTestUtil.getExpressionRange(39, 15, 39, 68)};

        TypesFromExpressionResponse typesFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                uri, ranges, this.serviceEndpoint);

        Assert.assertNotNull(typesFromExpression.getTypes());

        ResolvedTypeForExpression type = typesFromExpression.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(ranges[0], type.getRequestedRange()));
        Assert.assertTrue(type.getType() instanceof RecordType);

        RecordType recordType = (RecordType) type.getType();
        Assert.assertEquals(recordType.fields.size(), 2);
        Assert.assertEquals(recordType.fields.get(0).name, "id");
        Assert.assertEquals(recordType.fields.get(0).typeName, "string");
        Assert.assertEquals(recordType.fields.get(1).name, "batch");
        Assert.assertEquals(recordType.fields.get(1).typeName, "int");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for a function call expression node")
    public void testTypeForFunctionCallExprNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromExpressionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LineRange[] ranges = {SymbolServiceTestUtil.getExpressionRange(40, 39, 40, 53)};

        TypesFromExpressionResponse typesFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                uri, ranges, this.serviceEndpoint);

        Assert.assertNotNull(typesFromExpression.getTypes());

        ResolvedTypeForExpression type = typesFromExpression.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(ranges[0], type.getRequestedRange()));
        Assert.assertTrue(type.getType() instanceof ArrayType);

        ArrayType arrayType = (ArrayType) type.getType();
        Assert.assertTrue(arrayType.memberType instanceof RecordType);

        RecordType recordType = (RecordType) arrayType.memberType;
        Assert.assertEquals(recordType.fields.size(), 2);
        Assert.assertEquals(recordType.fields.get(0).name, "x");
        Assert.assertEquals(recordType.fields.get(0).typeName, "int");
        Assert.assertEquals(recordType.fields.get(1).name, "y");
        Assert.assertEquals(recordType.fields.get(1).typeName, "int");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "primitive type info retrieved for a function call node")
    public void testPrimitiveTypeForFunctionCallNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromExpressionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LineRange[] ranges = {SymbolServiceTestUtil.getExpressionRange(42, 24, 42, 57)};

        TypesFromExpressionResponse typesFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                uri, ranges, this.serviceEndpoint);

        Assert.assertNotNull(typesFromExpression.getTypes());

        ResolvedTypeForExpression type = typesFromExpression.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(ranges[0], type.getRequestedRange()));
        Assert.assertTrue(type.getType() instanceof PrimitiveType);
    }

    @Test(description = "type info retrieved for a field access expression node")
    public void testTypeForFieldAccessExprNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromExpressionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LineRange[] ranges = {SymbolServiceTestUtil.getExpressionRange(43, 40, 43, 50)};

        TypesFromExpressionResponse typesFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                uri, ranges, this.serviceEndpoint);

        Assert.assertNotNull(typesFromExpression.getTypes());

        ResolvedTypeForExpression type = typesFromExpression.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(ranges[0], type.getRequestedRange()));
        Assert.assertTrue(type.getType() instanceof ArrayType);

        ArrayType arrayType = (ArrayType) type.getType();
        Assert.assertTrue(arrayType.memberType instanceof RecordType);

        RecordType recordType = (RecordType) arrayType.memberType;
        Assert.assertEquals(recordType.fields.size(), 3);
        Assert.assertEquals(recordType.fields.get(0).name, "allJobs");
        Assert.assertEquals(recordType.fields.get(0).typeName, "int");
        Assert.assertEquals(recordType.fields.get(1).name, "failures");
        Assert.assertEquals(recordType.fields.get(1).typeName, "int");
        Assert.assertEquals(recordType.fields.get(2).name, "window");
        Assert.assertEquals(recordType.fields.get(2).typeName, "string");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "types info retrieved for multiple expression nodes")
    public void testTypeForMultipleExprNodes() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromExpressionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LineRange range1 = SymbolServiceTestUtil.getExpressionRange(39, 15, 39, 68);
        LineRange range2 = SymbolServiceTestUtil.getExpressionRange(40, 39, 40, 53);
        LineRange range3 = SymbolServiceTestUtil.getExpressionRange(42, 24, 42, 57);
        LineRange range4 = SymbolServiceTestUtil.getExpressionRange(43, 40, 43, 50);
        LineRange range5 = SymbolServiceTestUtil.getExpressionRange(47, 32, 47, 44);
        LineRange[] ranges = {range1, range2, range3, range4, range5};

        TypesFromExpressionResponse typesFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                uri, ranges, this.serviceEndpoint);

        Assert.assertNotNull(typesFromExpression.getTypes());

        ResolvedTypeForExpression type1 = typesFromExpression.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(range1, type1.getRequestedRange()));
        Assert.assertTrue(type1.getType() instanceof RecordType);

        ResolvedTypeForExpression type2 = typesFromExpression.getTypes().get(1);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(range2, type2.getRequestedRange()));
        Assert.assertTrue(type2.getType() instanceof ArrayType);

        ResolvedTypeForExpression type3 = typesFromExpression.getTypes().get(2);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(range3, type3.getRequestedRange()));
        Assert.assertTrue(type3.getType() instanceof PrimitiveType);

        ResolvedTypeForExpression type4 = typesFromExpression.getTypes().get(3);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(range4, type4.getRequestedRange()));
        Assert.assertTrue(type4.getType() instanceof ArrayType);

        ResolvedTypeForExpression type5 = typesFromExpression.getTypes().get(4);
        Assert.assertTrue(SymbolServiceTestUtil.isRangesEquals(range5, type5.getRequestedRange()));
        Assert.assertTrue(type5.getType() instanceof PrimitiveType);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test invalid file path")
    public void testInvalidFilePath() throws ExecutionException, InterruptedException {
        LineRange[] ranges = {SymbolServiceTestUtil.getExpressionRange(67, 19, 67, 45)};

        TypesFromExpressionResponse typesFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                URI.create("file://+"), ranges, this.serviceEndpoint);

        Assert.assertEquals(typesFromExpression.getTypes().size(), 0);
    }

    @Test(description = "test empty ranges")
    public void testEmptyRanges() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromExpressionBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        TypesFromExpressionResponse typeFromExpression = LSExtensionTestUtil.getTypeFromExpression(
                uri, null, this.serviceEndpoint);

        Assert.assertEquals(typeFromExpression.getTypes().size(), 0);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }
}
