/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
 * Test type info generated for types records which are having reoccurring types.
 */
public class ReoccurringTypesTest {
    private Endpoint serviceEndpoint;

    private final Path reoccurringTypesBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("reoccurringTypes.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "types that are reoccurring in the same record")
    public void testReoccurringTypes()
            throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(reoccurringTypesBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(13, 9);
        LinePosition returnTypeDescPosition = LinePosition.from(13, 37);

        TypesFromSymbolResponse typesResponse = LSExtensionTestUtil.getTypesFromFnDefinition(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesResponse.getTypes());

        ResolvedTypeForSymbol returnType = typesResponse.getTypes().get(0);
        Assert.assertEquals(returnType.getType().typeName, SymbolServiceTestUtil.RECORD);

        Assert.assertTrue(returnType.getType() instanceof RecordType);
        RecordType recordType = (RecordType) returnType.getType();
        Assert.assertEquals(recordType.fields.size(), 4);

        Assert.assertEquals(recordType.fields.get(0).name, "field1");
        Assert.assertEquals(recordType.fields.get(0).typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(recordType.fields.get(0).isOptional());
        ArrayType arrayType0 = (ArrayType) recordType.fields.get(0);
        assertIntOrStringType(arrayType0.memberType);

        Assert.assertEquals(recordType.fields.get(1).name, "field2");
        Assert.assertEquals(recordType.fields.get(1).typeName, SymbolServiceTestUtil.ARRAY);
        Assert.assertTrue(recordType.fields.get(1).isOptional());
        ArrayType arrayType1 = (ArrayType) recordType.fields.get(1);
        assertIntOrStringType(arrayType1.memberType);

        Assert.assertEquals(recordType.fields.get(2).name, "rec1");
        assertRecAType(recordType.fields.get(2));

        Assert.assertEquals(recordType.fields.get(3).name, "rec2");
        assertRecAType(recordType.fields.get(3));

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    private void assertIntOrStringType(Type type) {
        Assert.assertEquals(type.typeName, SymbolServiceTestUtil.UNION);
        UnionType unionType = (UnionType) type;
        Assert.assertEquals(unionType.name, "IntOrString");
        Assert.assertFalse(unionType.isRestType);
        Assert.assertEquals(unionType.members.size(), 2);
        Assert.assertEquals(unionType.members.get(0).typeName, SymbolServiceTestUtil.INTEGER);
        Assert.assertEquals(unionType.members.get(1).typeName, SymbolServiceTestUtil.STRING);
    }

    private void assertRecAType(Type type) {
        Assert.assertEquals(type.typeName, SymbolServiceTestUtil.RECORD);
        Assert.assertTrue(type.isOptional());
        RecordType recordType = (RecordType) type;
        Assert.assertEquals(recordType.fields.size(), 1);
        Assert.assertEquals(recordType.fields.get(0).name, "name");
        Assert.assertEquals(recordType.fields.get(0).typeName, SymbolServiceTestUtil.STRING);
    }
}
