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
 * Test type info retrieved via getTypesFromFnSignature endpoint.
 */
public class TypesFromFnSignatureTest {
    private Endpoint serviceEndpoint;

    private final Path typesFromFnSignatureBalFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("symbol")
            .resolve("typesFromFnSignature.bal");

    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "type info retrieved for the return type symbol node")
    public void testTypeForReturnTypeNode() throws IOException, ExecutionException, InterruptedException {
        Path inputFile = LSExtensionTestUtil.createTempFile(typesFromFnSignatureBalFile);
        URI uri = URI.create(inputFile.toUri().toString());
        TestUtil.openDocument(serviceEndpoint, inputFile);

        LinePosition fnPosition = LinePosition.from(67, 9);
        LinePosition returnTypeDescPosition = LinePosition.from(67, 55);

        TypesFromSymbolResponse typesFromSymbolResponse = LSExtensionTestUtil.getTypesFromFnSignature(
                uri, fnPosition, returnTypeDescPosition, this.serviceEndpoint);

        Assert.assertNotNull(typesFromSymbolResponse.getTypes());

        ResolvedTypeForSymbol type = typesFromSymbolResponse.getTypes().get(0);
        Assert.assertEquals(type.getType().name, "Output");
        Assert.assertTrue(SymbolServiceTestUtil.isPositionsEquals(fnPosition, type.getRequestedPosition()));
        Assert.assertTrue(type.getType() instanceof RecordType);

        RecordType recordType = (RecordType) type.getType();
        Assert.assertEquals(recordType.fields.size(), 2);

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }
}
