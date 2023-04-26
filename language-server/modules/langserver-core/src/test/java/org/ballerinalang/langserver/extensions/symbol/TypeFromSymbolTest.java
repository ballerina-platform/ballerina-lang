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
import org.ballerinalang.diagramutil.connector.models.connector.types.ArrayType;
import org.ballerinalang.diagramutil.connector.models.connector.types.PrimitiveType;
import org.ballerinalang.diagramutil.connector.models.connector.types.RecordType;
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
 * Test type info retrieved via getTypeFromSymbol endpoint.
 */
public class TypeFromSymbolTest {
    private Endpoint serviceEndpoint;

    private final Path typeFromSymbolBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("typeFromSymbol.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "type info retrieved for the return type symbol node")
    public void testTypeForReturnTypeNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromSymbolBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition position = LinePosition.from(67, 55);
        LinePosition[] positions = {position};
        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypeFromSymbol(
                uri, positions, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol type = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position, type.getRequestedPosition()));
        Assert.assertEquals(type.getType().name, "Output");
        Assert.assertTrue(type.getType() instanceof RecordType);

        RecordType recordType = (RecordType) type.getType();
        Assert.assertEquals(recordType.fields.size(), 2);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for the type name symbol of a required param node")
    public void testTypeForRequiredParamTypeNameNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromSymbolBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition position = LinePosition.from(67, 19);
        LinePosition[] positions = {position};
        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypeFromSymbol(
                uri, positions, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol type = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position, type.getRequestedPosition()));
        Assert.assertTrue(type.getType() instanceof RecordType);

        RecordType outerRecordType = (RecordType) type.getType();
        Assert.assertEquals(outerRecordType.fields.size(), 5);
        Assert.assertTrue(outerRecordType.fields.get(1) instanceof ArrayType);

        ArrayType arrayType = (ArrayType) outerRecordType.fields.get(1);
        Assert.assertTrue(arrayType.memberType instanceof RecordType);

        RecordType innerRecordType = (RecordType) arrayType.memberType;
        Assert.assertEquals(innerRecordType.fields.size(), 3);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for a let variable")
    public void testTypeForLetVarDeclaration() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromSymbolBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition position = LinePosition.from(67, 73);
        LinePosition[] positions = {position};
        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypeFromSymbol(
                uri, positions, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol type = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position, type.getRequestedPosition()));
        Assert.assertTrue(type.getType() instanceof PrimitiveType);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "type info retrieved for multiple symbols")
    public void testTypesForMultipleSymbols() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromSymbolBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition position1 = LinePosition.from(67, 19);
        LinePosition position2 = LinePosition.from(67, 32);
        LinePosition position3 = LinePosition.from(69, 8);
        LinePosition position4 = LinePosition.from(70, 12);
        LinePosition position5 = LinePosition.from(77, 8);
        LinePosition[] positions = {position1, position2, position3, position4, position5};
        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypeFromSymbol(
                uri, positions, this.serviceEndpoint);

        Assert.assertNotEquals(typesFromSymbolResponse.getTypes(), null);
        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), positions.length);

        ResolvedTypeForSymbol type1 = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position1, type1.getRequestedPosition()));
        Assert.assertTrue(type1.getType() instanceof RecordType);
        Assert.assertEquals(type1.getType().name, "Input");

        ResolvedTypeForSymbol type2 = typesFromSymbolResponse.getTypes().get(1);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position2, type2.getRequestedPosition()));
        Assert.assertTrue(type2.getType() instanceof RecordType);
        Assert.assertEquals(type2.getType().name, "Input2");

        ResolvedTypeForSymbol type3 = typesFromSymbolResponse.getTypes().get(2);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position3, type3.getRequestedPosition()));
        Assert.assertTrue(type3.getType() instanceof RecordType);

        ResolvedTypeForSymbol type4 = typesFromSymbolResponse.getTypes().get(3);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position4, type4.getRequestedPosition()));
        Assert.assertTrue(type4.getType() instanceof ArrayType);

        ResolvedTypeForSymbol type5 = typesFromSymbolResponse.getTypes().get(4);
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(position5, type5.getRequestedPosition()));
        Assert.assertTrue(type5.getType() instanceof PrimitiveType);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "test invalid file path")
    public void testInvalidFilePath() throws ExecutionException, InterruptedException {
        LinePosition position = LinePosition.from(67, 19);
        LinePosition[] positions = {position};

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypeFromSymbol(
                URI.create("file://+"), positions, this.serviceEndpoint);

        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), 0);
    }

    @Test(description = "test empty positions")
    public void testEmptyPositions() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typeFromSymbolBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypeFromSymbol(
                uri, null, this.serviceEndpoint);

        Assert.assertEquals(typesFromSymbolResponse.getTypes().size(), 0);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }
}
