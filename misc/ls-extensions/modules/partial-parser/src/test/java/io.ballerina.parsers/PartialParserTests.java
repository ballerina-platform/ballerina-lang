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
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for PartialParser.
 */
public class PartialParserTests {
    private static final String SINGLE_STATEMENT = "partialParser/getSTForSingleStatement";
    private static final String EXPRESSION = "partialParser/getSTForExpression";
    private static final String MODULE_MEMBER = "partialParser/getSTForModuleMembers";

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    private final Path singleStatementST = RES_DIR.resolve("syntax-tree").resolve("single_statement.json");
    private final Path singleStatementModifiedWithReplaceST = RES_DIR.resolve("syntax-tree")
            .resolve("single_statement_replace_modification.json");
    private final Path singleStatementModifiedWithDeleteST = RES_DIR.resolve("syntax-tree")
            .resolve("single_statement_delete_modification.json");
    private final Path singleStatementModifiedWithInsertST = RES_DIR.resolve("syntax-tree")
            .resolve("single_statement_insert_modification.json");
    private final Path expressionST = RES_DIR.resolve("syntax-tree").resolve("expression.json");
    private final Path sampleRecord = RES_DIR.resolve("ballerina").resolve("sample_record.bal");
    private final Path recordST = RES_DIR.resolve("syntax-tree").resolve("sample_record.json");

    private Endpoint serviceEndpoint;

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test getting ST for a single statement")
    public void testSTForSingleStatement() throws ExecutionException, InterruptedException, FileNotFoundException {
        String statement = "string fullName = firstName + \"Cooper\";";
        PartialSTRequest request = new PartialSTRequest(statement);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(new FileReader(singleStatementST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a single statement with replace modification")
    public void testModifiedSTForReplacement()
            throws ExecutionException, InterruptedException, FileNotFoundException {
        String statement = "string fullName = firstName + \"Cooper\";";
        String modification = "middleName + \"Hofstadter\"";
        STModification stModification = new STModification(0, 30, 0, 38, modification);
        PartialSTRequest request = new PartialSTRequest(statement, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(
                new FileReader(singleStatementModifiedWithReplaceST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a single statement with delete modification")
    public void testModifiedSTForDeletion()
            throws ExecutionException, InterruptedException, FileNotFoundException {
        String statement = "string fullName = firstName + \"Cooper\";";
        String modification = "";
        STModification stModification = new STModification(0, 16, 0, 38, modification);
        PartialSTRequest request = new PartialSTRequest(statement, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(
                new FileReader(singleStatementModifiedWithDeleteST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a single statement with insert modification")
    public void testModifiedSTForInsertion()
            throws ExecutionException, InterruptedException, FileNotFoundException {
        String statement = "";
        String modification = "int age = 15;";
        STModification stModification = new STModification(0, 0, 0, 0, modification);
        PartialSTRequest request = new PartialSTRequest(statement, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(
                new FileReader(singleStatementModifiedWithInsertST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for an expression")
    public void testSTForExpression() throws ExecutionException, InterruptedException, FileNotFoundException {
        String expression = "(population - infantCount) * 20;";
        PartialSTRequest request = new PartialSTRequest(expression);
        CompletableFuture<?> result = serviceEndpoint.request(EXPRESSION, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(new FileReader(expressionST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a record")
    public void testSTForRecordMembers() throws ExecutionException, InterruptedException, IOException {
        String recordMember =  Files.readString(sampleRecord);;
        PartialSTRequest request = new PartialSTRequest(recordMember);
        CompletableFuture<?> result = serviceEndpoint.request(MODULE_MEMBER, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(new FileReader(recordST.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

}
