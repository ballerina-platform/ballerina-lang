/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.path.nativeimpl;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbsoluteTest {
    private CompileResult fileOperationProgramFile;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final Logger log = LoggerFactory.getLogger(AbsoluteTest.class);

    @BeforeMethod
    public void setup() {
        fileOperationProgramFile = BCompileUtil.compile("test-src/absolute_test.bal");
        Diagnostic[] diagnostics = fileOperationProgramFile.getDiagnostics();
        for (Diagnostic diag : diagnostics) {
            log.error(diag.getMessage());
        }
    }

    @Test(description = "Test path separator value")
    public void testPathSeparator() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetPathSeparator", args);
        BString pathSeparator = (BString) returns[0];
        log.info("Ballerina Path: separator value. Return value: " + pathSeparator.stringValue());
        assertEquals(pathSeparator.stringValue(), File.separator);
    }

    @Test(description = "Test path list separator value")
    public void testPathListSeparator() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetPathListSeparator", args);
        BString listSeparator = (BString) returns[0];
        log.info("Ballerina Path: list separator value. Return value: " + listSeparator.stringValue());
        assertEquals(listSeparator.stringValue(), File.pathSeparator);
    }

    @Test(description = "Test absolute path function")
    public void testGetAbsolutePath() {
        String inputPath = "test.txt";
        BValue[] args = {new BString(inputPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetAbsolutePath", args);
        BString absPath = (BString) returns[0];
        log.info("Ballerina Path: absolute function. Return value: " + absPath.stringValue());
        assertEquals(absPath.stringValue(), Paths.get(inputPath).toAbsolutePath().toString());
    }

    @Test(description = "Test absolute path function")
    public void testAbsolutePath() {
        String inputPath = "/test.txt";
        BValue[] args = {new BString(inputPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetAbsolutePath", args);
        BString absPath = (BString) returns[0];
        log.info("Ballerina Path: absolute function. Return value: " + absPath.stringValue());
        assertEquals(absPath.stringValue(), Paths.get(inputPath).toAbsolutePath().toString());
    }

    @Test(description = "Test illegal windows path conversion")
    public void testIllegalWindowsPath() {
        String illegal = "/C:/Users/Desktop/workspaces/dk/ballerina/stdlib-path/target/test-classes/absolute" +
                "\\swagger.json";
        BValue[] args = {new BString(illegal)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetAbsolutePath", args);
        if (IS_WINDOWS) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.reason, "{ballerina/path}INVALID_PATH");
        } else {
            BString absPath = (BString) returns[0];
            log.info("Ballerina Path: absolute function. Return value: " + absPath.stringValue());
            assertEquals(absPath.stringValue(), Paths.get(illegal).toAbsolutePath().toString());
        }
    }

    @Test(description = "Test isAbsolute path function for posix paths", dataProvider = "posix_paths")
    public void testPosixAbsolutePath(String path) {
        validateAbsolutePath(path);
    }

    @Test(description = "Test isAbsolute path function for windows paths", dataProvider = "windows_paths")
    public void testWindowsAbsolutePath(String path) {
        validateAbsolutePath(path);
    }

    private void validateAbsolutePath(String input) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testIsAbsolutePath", args);
        BBoolean isAbs = (BBoolean) returns[0];
        log.info("Ballerina Path: isAbsolute function. " + input + " is absolute: " + isAbs.booleanValue());
        assertEquals(isAbs.booleanValue(), Paths.get(input).isAbsolute());
    }


    @Test(description = "Test filename path function for posix paths", dataProvider = "posix_paths")
    public void testGetPosixFileName(String path) {
        validateFilename(path);
    }

    private void validateFilename(String input) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetFilename", args);
        BString filename = (BString) returns[0];
        log.info("Ballerina Path: filename function. Return value: " + filename.stringValue());
        assertEquals(filename.stringValue(), Paths.get(input).getFileName().toString());
    }

    @DataProvider(name = "posix_paths")
    public Object[] getPosixPaths() {
        return new Object[] {
                "/A/B/C",
                "/foo/..",
                ".",
                "foo/",
                "foo/bar/"
        };
    }

    @DataProvider(name = "windows_paths")
    public Object[] getWindowsPaths() {
        return new Object[] {
                "//server",
                "\\\\server",
                "C:/foo/..",
                "C:\\foo\\..",
                "bar\\baz",
                "bar/baz",
                "."
        };
    }
}
