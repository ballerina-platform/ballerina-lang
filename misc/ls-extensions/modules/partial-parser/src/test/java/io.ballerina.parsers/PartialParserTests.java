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
    private static final String MODULE_PART = "partialParser/getSTForModulePart";
    private static final String RESOURCE = "partialParser/getSTForResource";

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final Path ST_WINDOWS = RES_DIR.resolve("syntax-tree").resolve("windows");
    private static final Path ST_LINUX = RES_DIR.resolve("syntax-tree").resolve("linux");

    private final Path sampleRecord = RES_DIR.resolve("ballerina").resolve("sample_record.bal");
    private final Path sampleServiceNListener = RES_DIR.resolve("ballerina").resolve("sample_service_and_listener.bal");
    private final Path sampleResource = RES_DIR.resolve("ballerina").resolve("sample_resource.bal");

    private Endpoint serviceEndpoint;

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test getting ST for a single line statement")
    public void testSTForSingleLineStatement() throws ExecutionException, InterruptedException, FileNotFoundException {

        String statement = "string fullName = firstName + \"Cooper\";";
        String file = "single_line_statement.json";

        PartialSTRequest request = new PartialSTRequest(statement);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a multi line statement")
    public void testSTForMultiLineStatement() throws ExecutionException, InterruptedException, FileNotFoundException {

        String statement = "\nforeach var item in arr {\n            io:println(item);\n\n}";
        String file = "multi_line_statement.json";

        PartialSTRequest request = new PartialSTRequest(statement);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a single statement with replace modification")
    public void testModifiedSTForReplacement() throws ExecutionException, InterruptedException, FileNotFoundException {

        String statement = "string fullName = firstName + \"Cooper\";";
        String modification = "middleName + \"Hofstadter\"";
        String file = "single_statement_replace_modification.json";

        STModification stModification = new STModification(0, 30, 0, 38, modification);
        PartialSTRequest request = new PartialSTRequest(statement, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a single statement with delete modification")
    public void testModifiedSTForDeletion() throws ExecutionException, InterruptedException, FileNotFoundException {

        String statement = "string fullName = firstName + \"Cooper\";";
        String modification = "";
        String file = "single_statement_delete_modification.json";

        STModification stModification = new STModification(0, 16, 0, 38, modification);
        PartialSTRequest request = new PartialSTRequest(statement, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a single statement with insert modification")
    public void testModifiedSTForInsertion() throws ExecutionException, InterruptedException, FileNotFoundException {

        String statement = "";
        String modification = "int age = 15;";
        String file = "single_statement_insert_modification.json";

        STModification stModification = new STModification(0, 0, 0, 0, modification);
        PartialSTRequest request = new PartialSTRequest(statement, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(SINGLE_STATEMENT, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for an expression")
    public void testSTForExpression() throws ExecutionException, InterruptedException, FileNotFoundException {

        String expression = "(population - infantCount) * 20;";
        String file = "expression.json";

        PartialSTRequest request = new PartialSTRequest(expression);
        CompletableFuture<?> result = serviceEndpoint.request(EXPRESSION, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for an expression with insert modification")
    public void testModifiedSTForExpression() throws ExecutionException, InterruptedException, FileNotFoundException {
        String expression = "";
        String modification = "(population - infantCount) * 20;";
        String file = "expression.json";

        STModification stModification = new STModification(0, 0, 0, 0, modification);
        PartialSTRequest request = new PartialSTRequest(expression, stModification);
        CompletableFuture<?> result = serviceEndpoint.request(EXPRESSION, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a record")
    public void testSTForRecordMembers() throws ExecutionException, InterruptedException, IOException {

        String recordMember =  Files.readString(sampleRecord);
        String file = "sample_record.json";

        PartialSTRequest request = new PartialSTRequest(recordMember);
        CompletableFuture<?> result = serviceEndpoint.request(MODULE_MEMBER, request);
        STResponse json = (STResponse) result.get();

        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a module part")
    public void testSTForModulePart() throws ExecutionException, InterruptedException, IOException {
        String file = "sample_service_and_listener.json";

        String modulePart =  Files.readString(sampleServiceNListener);;
        PartialSTRequest request = new PartialSTRequest(modulePart);
        CompletableFuture<?> result = serviceEndpoint.request(MODULE_PART, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    @Test(description = "Test getting ST for a resource")
    public void testSTForResource() throws ExecutionException, InterruptedException, IOException {
        String file = "sample_resource.json";

        String modulePart =  Files.readString(sampleResource);;
        PartialSTRequest request = new PartialSTRequest(modulePart);
        CompletableFuture<?> result = serviceEndpoint.request(RESOURCE, request);
        STResponse json = (STResponse) result.get();
        BufferedReader br = new BufferedReader(getFileReader(file));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        Assert.assertEquals(json.getSyntaxTree(), expected);
    }

    private FileReader getFileReader(String fileName) throws FileNotFoundException {
        FileReader fileReader;
        if (System.getProperty("os.name").startsWith("Windows")) {
            fileReader = new FileReader(ST_WINDOWS.resolve(fileName).toAbsolutePath().toString());
        } else {
            fileReader = new FileReader(ST_LINUX.resolve(fileName).toAbsolutePath().toString());
        }
        return fileReader;
    }

}
