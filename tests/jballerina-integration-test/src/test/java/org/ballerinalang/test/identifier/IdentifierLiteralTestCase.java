/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.identifier;

import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Test case for the ballerina supporting quoted identifiers.
 */
public class IdentifierLiteralTestCase extends BaseTest {

    private static BServerInstance serverInstance;
    private static final String testFileLocation = Paths.get("src", "test", "resources", "identifier")
            .toAbsolutePath().toString();
    private static final String identifierPositiveTestFileName = "identifier_literal_positive.bal";
    private static final String invalidILSpecialCharactersFileName = "invalid_IL_special_char.bal";
    private static final String invalidILEscapeCharactersFileName = "invalid_IL_escape_char.bal";
    private static final String invalidILUnicodeCharactersFileName = "invalid_IL_unicode_char.bal";

    @BeforeClass(enabled = false)
    public void setup() throws BallerinaTestException {
        int[] requiredPorts = new int[]{9090};
        Utils.checkPortsAvailability(requiredPorts);
        String balFilePath =
                new File(testFileLocation + File.separator + "identifier_literal_service.bal").getAbsolutePath();
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFilePath, requiredPorts);
    }

    @AfterClass(enabled = false)
    private void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

    @Test(enabled = false, description = "Tests quoted identifier literal with allowed characters")
    public void testILpositive() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{identifierPositiveTestFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 3);
        assertEquals(logLines[0], "Compiling source");
        assertEquals(logLines[1], "\tidentifier_literal_positive.bal");
        assertEquals(logLines[2], "Running executables");
    }

    @Test(enabled = false, description = "Tests quoted identifier literal containing invalid special characters")
    public void testInvalidILSpecialChar() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{invalidILSpecialCharactersFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");
        String expectedError = "Compiling source\n" +
                "\tinvalid_IL_special_char.bal\n" +
                "error: .::invalid_IL_special_char.bal:18:14: missing semicolon token\n" +
                "error: .::invalid_IL_special_char.bal:18:14: missing type desc\n" +
                "error: .::invalid_IL_special_char.bal:18:33: missing equal token\n" +
                "error: .::invalid_IL_special_char.bal:18:38: missing plus token\n" +
                "error: .::invalid_IL_special_char.bal:18:38: missing double quote\n" +
                "error: .::invalid_IL_special_char.bal:19:1: missing semicolon token\n" +
                "error: .::invalid_IL_special_char.bal:19:15: missing double quote\n" +
                "error: .::invalid_IL_special_char.bal:20:1: missing semicolon token\n" +
                "error: .::invalid_IL_special_char.bal:18:5: no new variables on left side\n" +
                "error: .::invalid_IL_special_char.bal:18:14: invalid intersection type " +
                "'$missingNode$0 & *%_var = ': no intersection\n" +
                "error: .::invalid_IL_special_char.bal:18:33: undefined symbol 'value'\n" +
                "error: .::invalid_IL_special_char.bal:19:12: underscore is not allowed here";
        assertErrorLines(logLines, expectedError);
    }

    @Test(enabled = false, description = "Tests quoted identifier literal containing invalid escape characters")
    public void testInvalidILEscapeChar() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{invalidILEscapeCharactersFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");
        String expectedError = "Compiling source" +
                "\n\tinvalid_IL_escape_char.bal" +
                "\nerror: .::invalid_IL_escape_char.bal:18:12: invalid escape sequence '\\a'" +
                "\nerror: .::invalid_IL_escape_char.bal:18:12: invalid escape sequence '\\B'" +
                "\nerror: .::invalid_IL_escape_char.bal:18:12: invalid escape sequence '\\c'" +
                "\nerror: .::invalid_IL_escape_char.bal:18:12: invalid escape sequence '\\x'" +
                "\nerror: .::invalid_IL_escape_char.bal:18:12: invalid escape sequence '\\y'" +
                "\nerror: .::invalid_IL_escape_char.bal:18:12: invalid escape sequence '\\z'" +
                "\nerror: .::invalid_IL_escape_char.bal:19:12: invalid escape sequence '\\a'" +
                "\nerror: .::invalid_IL_escape_char.bal:19:12: invalid escape sequence '\\B'" +
                "\nerror: .::invalid_IL_escape_char.bal:19:12: invalid escape sequence '\\c'" +
                "\nerror: .::invalid_IL_escape_char.bal:19:12: invalid escape sequence '\\x'" +
                "\nerror: .::invalid_IL_escape_char.bal:19:12: invalid escape sequence '\\y'" +
                "\nerror: .::invalid_IL_escape_char.bal:19:12: invalid escape sequence '\\z'";
        assertErrorLines(logLines, expectedError);
    }

    @Test(enabled = false, description = "Tests quoted identifier literal containing invalid unicode characters")
    public void testInvalidILUnicodeChar() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{invalidILUnicodeCharactersFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");
        String expectedError = "Compiling source\n" +
                "\tinvalid_IL_unicode_char.bal\n" +
                "error: .::invalid_IL_unicode_char.bal:18:28: invalid token 'whiteSpace'\n" +
                "error: .::invalid_IL_unicode_char.bal:19:17: missing plus token\n" +
                "error: .::invalid_IL_unicode_char.bal:19:12: undefined symbol ''\n" +
                "error: .::invalid_IL_unicode_char.bal:19:17: undefined symbol 'whiteSpace'";

        assertErrorLines(logLines, expectedError);
    }

    @Test(enabled = false, description = "Test using identifier literals in service and resource names")
    public void testUsingIdentifierLiteralsInServiceAndResourceNames() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9090, "identifierLiteral" +
                "/resource1"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        BValue bJson = JsonParser.parse(response.getData());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("key").stringValue(), "keyVal");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("value").stringValue(), "valueOfTheString");
    }

    @Test(enabled = false, description = "Test identifier literals in payload")
    public void testIdentifierLiteralsInPayload() throws IOException {

        Map<String, String> headers = new HashMap<>();
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9090, "identifierLiteral" +
                "/resource2"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "hello");
    }

    @Test(enabled = false, description = "Test accessing variable in other packages defined with identifier literal")
    public void testAccessingVarsInOtherPackage() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "TestProject")
                .toAbsolutePath();
        String runLog = "Values returned successfully";
        LogLeecher runLeecher = new LogLeecher(runLog);
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain(projectPath.toString(), "pkg.main", new LogLeecher[]{runLeecher});
        Assert.assertTrue(runLeecher.isTextFound());
    }

    private void assertErrorLines(String[] logLines, String expectedError) {
        String[] expectedErrorLines = expectedError.split("\n");
        Assert.assertEquals(logLines.length, expectedErrorLines.length);
        Assert.assertEquals(logLines, expectedErrorLines);
    }
}
