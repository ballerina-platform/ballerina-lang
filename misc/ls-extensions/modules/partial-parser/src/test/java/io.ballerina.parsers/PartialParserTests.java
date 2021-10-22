/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.parsers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.ballerinalang.langserver.util.TestUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for PartialParser.
 */
public class PartialParserTests {
    private static final String SINGLE_STATEMENT = "partialParser/getSTForSingleStatement";

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    private final Path singleStatementST = RES_DIR.resolve("syntax-tree")
            .resolve("single_statement.json");

    private Endpoint serviceEndpoint;

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test getting ST for a single statement")
    public void testSTForSingleStatement() throws ExecutionException, InterruptedException, FileNotFoundException {
        String statement = "string x = aa;";
        PartialSTRequest request = new PartialSTRequest(statement);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(new FileReader(singleStatementST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

}
