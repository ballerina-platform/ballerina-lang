/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.entry.function;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

/**
 * Test class for entry function argument parsing positive scenarios.
 *
 * @since 0.982.0
 */
public class ArgumentParserPositiveTest {

    private static final String FILE_NAME = "test_entry_function.bal";
    private ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
    private PrintStream defaultOut;
    private ProgramFile programFile;

    @BeforeClass
    public void setUp() {
        defaultOut = System.out;
        programFile = LauncherUtils.compile(Paths.get("src/test/resources/test-src/entry.function"),
                                                        Paths.get(FILE_NAME), false);
    }

    @Test
    public void testNoArg() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "noParamEntry", new String[]{});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "1", "invalid return value");
    }

    @Test (dataProvider = "intValues")
    public void testIntArg(String specifiedInt, String expectedInt) throws IOException {
        resetTempOut();
        BLangProgramRunner.runEntryFunc(programFile, "intEntry", new String[]{specifiedInt});
        Assert.assertEquals(tempOutStream.toString(), expectedInt, "string arg parsed as invalid int");
    }

    @Test (dataProvider = "jsonValues")
    public void testJsonArg(String arg) {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "jsonEntry", new String[]{arg});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), arg, "invalid JSON return value");
    }

    @Test
    public void testXmlArg() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(
                programFile, "xmlEntry", new String[]{"<book status=\"available\" count=\"5\"/>"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "<book status=\"available\" count=\"5\"></book>",
                            "invalid XML return value");
    }

    @Test (dataProvider = "typedescValues")
    public void testTypedescArg(String arg) {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "typedescEntry", new String[]{arg});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), arg, "invalid typedesc return value");
    }

    @Test (dataProvider = "arrayValues")
    public void testValidArrayArg(String arg) {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "arrayUnionEntry", new String[]{arg});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), arg, "invalid array return value");
    }

    @Test
    public void testTupleArg() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "tupleEntry",
                                                                   new String[]{"(101, {\"name\":\"Maryam\"}, "
                                                                           + "\"finance\")"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "Id: 101, Name: Maryam, Dept: finance",
                            "invalid return value with tuple args");
    }


    @Test
    public void testAllNamedArgs() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "defaultableParamEntry",
                                                                   new String[]{"-i=2", "-s=hi", "false", "is"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "2 hi world: is false", "invalid return value "
                + "with all named args");
    }

    @Test
    public void testSingleNamedArg() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "defaultableParamEntry",
                                                                   new String[]{"-s=hi", "false", "is"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "1 hi world: is false", "invalid return value "
                + "with single named arg");
    }

    @Test
    public void testNoNamedArg() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "defaultableParamEntry",
                                                                   new String[]{"true", "is"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "1 default hello world: is true",
                          "invalid return value with no named args");
    }

    @Test
    public void testMultipleParam() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "combinedTypeEntry",
                                                                   new String[]{"1000", "1.0", "Hello Ballerina", "255",
                                                                           "true", "{ \"name\": \"Maryam\" }",
                                                                           "<book>Harry Potter</book>",
                                                                           "{ \"name\": \"Em\" }",
                                                                           "just", "the", "rest"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "integer: 1000, float: 1.0, string: Hello Ballerina, "
                                                                          + "byte: 255, boolean: true, "
                                                                          + "JSON Name Field: Maryam, "
                                                                          + "XML Element Name: "
                                                                          + "book, Employee Name Field: Em, "
                                                                          + "string rest args: just the rest ",
                          "invalid return value with multiple typed args");
    }

    @Test
    public void testOneDimArrParam() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "oneDimensionalArrayEntry",
                                                                   new String[]{"[1, 200, 3]",
                                                                           "[\"hello\", \"ballerina\"]",
                                                                           "[1.0, 20.4, 30.3]", "[true, true, false]",
                                                                           "[5, \"Maryam\", { \"name\": \"Maryam\" }]",
                                                                           "[{ \"name\": \"Maryam\" }, "
                                                                                   + "{ \"name\": \"Em\" }, "
                                                                                   + "{ \"name\": \"Ziyad\" }]"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "integer: 200, string: ballerina, float: 20.4, "
                                                                          + "boolean: true, json: Maryam, "
                                                                          + "Employee Name Field: Em",
                          "invalid return value with one dimensional array param");
    }

    @Test
    public void testMapParam() {
        BValue[] entryFuncResult = BLangProgramRunner.runEntryFunc(programFile, "mapEntry",
                                                                   new String[]{"{\"int\":10, \"test\":120}",
                                                                           "{\"test\":\"Ballerina\", "
                                                                                   + "\"string\":\"str\"}",
                                                                           "{\"test\":12.0, \"float\":11.1}",
                                                                           "{\"boolean\":false, \"test\":true}",
                                                                           "{\"test\":5, \"json\":"
                                                                                   + "{ \"name\": \"Maryam\" }}",
                                                                           "{\"test\": { \"name\": \"Maryam\" }, "
                                                                                   + "\"record\": {\"name\":\"Em\"}}"});
        Assert.assertTrue(entryFuncResult != null && entryFuncResult.length == 1, "return value not available");
        Assert.assertEquals(entryFuncResult[0].stringValue(), "integer: 120, string: Ballerina, float: 12.0, "
                                                                          + "boolean: true, json: 5, "
                                                                          + "Test Employee Name Field: Maryam",
                          "invalid return value with map args");
    }

    @AfterClass
    public void tearDown() throws IOException {
        tempOutStream.close();
        System.setOut(defaultOut);
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][] {
                { "10", "10" },
                { "0b1101", "13" },
                { "0B110101", "53" },
                { "0x1efa2", "126882" },
                { "0XFAF1", "64241" }
        };
    }

    @DataProvider(name = "jsonValues")
    public Object[][] jsonValues() {
        return new Object[][] {
                { "10" },
                { "1.0" },
                { "true" },
                { "{\"name\":\"Maryam\"}" }
        };
    }

    @DataProvider(name = "typedescValues")
    public Object[][] typedescValues() {
        return new Object[][] {
                { "string" },
                { "int" },
                { "float" },
                { "byte" },
                { "boolean" },
                { "json" },
                { "xml" },
                { "map" },
                { "future" },
                { "table" },
                { "stream" },
                { "any" },
                { "typedesc" }
        };
    }

    @DataProvider(name = "arrayValues")
    public Object[][] arrayValues() {
        return new Object[][] {
                { "[1, 2, 3]" },
                { "[1.1, 1.2, 1.3]" },
                { "[true, false, true]" },
                { "[{\"name\":\"Maryam\"}, {\"address\":\"Colombo\"}]" }
        };
    }

    private void resetTempOut() throws IOException {
        tempOutStream.close();
        tempOutStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tempOutStream));
    }
}
