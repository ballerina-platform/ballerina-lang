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
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test path functions in ballerina:path.
 *
 * @since 0.995.0
 */
public class PathTest {
    private CompileResult fileOperationProgramFile;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final Logger log = LoggerFactory.getLogger(PathTest.class);

    @BeforeTest
    public void setup() {
        Path sourceFilePath = Paths.get("src", "test", "resources", "test-src", "path_test.bal");
        fileOperationProgramFile = BCompileUtil.compile(sourceFilePath.toAbsolutePath().toString());
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
        log.info("{ballerina/path}:absolute(). Return value: " + absPath.stringValue());
        assertEquals(absPath.stringValue(), Paths.get(inputPath).toAbsolutePath().toString());
    }

    @Test(description = "Test absolute path function")
    public void testAbsolutePath() {
        String inputPath = "/test.txt";
        BValue[] args = {new BString(inputPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetAbsolutePath", args);
        BString absPath = (BString) returns[0];
        log.info("{ballerina/path}:absolute(). Return value: " + absPath.stringValue());
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
            log.info("{ballerina/path}:absolute(). Return value: " + absPath.stringValue());
            assertEquals(absPath.stringValue(), Paths.get(illegal).toAbsolutePath().toString());
        }
    }

    @Test(description = "Test isAbsolute path function for posix paths", dataProvider = "is_absolute_data")
    public void testAbsolutePath(String path, boolean posixOutput, boolean windowsOutput) {
        if (IS_WINDOWS) {
            validateAbsolutePath(path, windowsOutput);
        } else {
            validateAbsolutePath(path, posixOutput);
        }
    }

    // @Test(description = "Test isAbsolute path function for windows paths", dataProvider = "windows_paths")
    // public void testWindowsAbsolutePath(String path) {
    //     validateAbsolutePath(path);
    // }

    private void validateAbsolutePath(String input, boolean expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testIsAbsolutePath", args);
        assertTrue(returns[0] instanceof BBoolean);
        BBoolean isAbs = (BBoolean) returns[0];
        boolean actual = isAbs.booleanValue();
        log.info("{ballerina/path}:isAbsolute(). Input: " + input + " is absolute: " + actual);
        assertEquals(actual, expected, "Path: " + input + " isAbsolute()");
        try {
            assertEquals(actual, Paths.get(input).isAbsolute(), "Assert with Java | Path: " + input + " isAbsolute()");
        } catch (InvalidPathException ex) {
            assertFalse(actual, "Path: " + input + " isAbsolute()");
        }
    }


    @Test(description = "Test filename path function for posix paths", dataProvider = "filename_data")
    public void testGetFileName(String path, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            validateFilename(path, windowsOutput);
        } else {
            validateFilename(path, posixOutput);
        }
    }

    private void validateFilename(String path, String expected) {
        BValue[] args = {new BString(path)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetFilename", args);
        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/path}INVALID_UNC_PATH");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BString);
            BString filename = (BString) returns[0];
            log.info("{ballerina/path}:filename(). Input: " + path + " | Return: " + filename.stringValue());
            assertEquals(filename.stringValue(), expected, "Path: " + path + " filename()");
            String expectedValue = Paths.get(path).getFileName() != null ? 
                                    Paths.get(path).getFileName().toString() : "";
            assertEquals(filename.stringValue(), expectedValue, "Assert with Java | Path: " + path + " filename()");
        }
    }

    @Test(description = "Test parent path function for paths", dataProvider = "parent_data")
    public void testGetParent(String path, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            validateParent(path, windowsOutput);
        } else {
            validateParent(path, posixOutput);
        }
    }

    private void validateParent(String input, String expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetParent", args);
        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/path}INVALID_UNC_PATH");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BString);
            BString parent = (BString) returns[0];
            log.info("{ballerina/path}:parent(). Input: " + input + " | Return: " + parent.stringValue());
            assertEquals(parent.stringValue(), expected, "Path: " + input + " filename()");
            String expectedValue = Paths.get(input).getParent() != null ?
                    Paths.get(input).getParent().toString() : "";
            assertEquals(parent.stringValue(), expectedValue, "Assert with Java | Path: " + input + " parent()");
        }
    }

    //@Test(description = "Test normalize path function for posix paths", dataProvider = "posix_paths")
    public void testPosixNormalizePath(String path) {
        validateNormalizePath(path);
    }

    //@Test(description = "Test normalize path function for windows paths", dataProvider = "windows_paths")
    public void testWindowsNormalizePath(String path) {
        validateNormalizePath(path);
    }

    private void validateNormalizePath(String input) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testNormalizePath", args);
        BString filename = (BString) returns[0];
        log.info("{ballerina/path}:normalize(). Input: " + input + " | Return: " + filename.stringValue());
        String expectedValue = Paths.get(input).normalize() != null ? Paths.get(input).normalize().toString() : "";
        assertEquals(filename.stringValue(), expectedValue);
    }

    //@Test(description = "Test split path function for posix paths", dataProvider = "posix_paths")
    public void testPosixSplitPath(String path) {
        validateSplitPath(path);
    }

    //@Test(description = "Test split path function for windows paths", dataProvider = "windows_paths")
    public void testWindowsSplitPath(String path) {
        validateSplitPath(path);
    }

    private void validateSplitPath(String input) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testSplitPath", args);
        BValueArray parts = (BValueArray) returns[0];
        log.info("{ballerina/path}:split(). Input: " + input + " | Return: " + parts.stringValue());
        int expectedSize = Paths.get(input) != null ? Paths.get(input).getNameCount() : 0;
        assertEquals(parts.size(), expectedSize);
    }

    //@Test(description = "Test build path function for paths", dataProvider = "file_parts")
    public void testBuildPath(String... parts) {
        validateBuildPath(parts);
    }

    private void validateBuildPath(String[] parts) {
        BValueArray valueArray = new BValueArray(BTypes.typeString);
        int i = 0;
        for (String part: parts) {
            valueArray.add(i++, part);
        }
        BValue[] args = {valueArray};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testBuildPath", args);
        BString resultPath = (BString) returns[0];
        log.info("{ballerina/path}:build(). Input: " + Arrays.asList(parts) + " | Return: " + resultPath);
        Path expectedPath = Paths.get(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
        String expectedValue =  expectedPath != null ? expectedPath.toString() : "";
        assertEquals(resultPath.stringValue(), expectedValue);
    }

    //@Test(description = "Test extension path function for posix paths", dataProvider = "ext_parts")
    public void testPathExtension(String path, String expected) {
        validateFileExtension(path, expected);
    }

    private void validateFileExtension(String input, String expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testPathExtension", args);
        BString extension = (BString) returns[0];
        log.info("{ballerina/path}:extension(). Input: " + input + " | Return: " + extension.stringValue());
        assertEquals(extension.stringValue(), expected);
    }

    //@Test(description = "Test relative path function for posix paths", dataProvider = "relative_tests")
    public void testPosixRelativePath(String basePath, String targetPath, String expected) {
        validateRelativePath(basePath, targetPath, expected);
    }

    private void validateRelativePath(String basePath, String targetPath, String expected) {
        BValue[] args = {new BString(basePath), new BString(targetPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testRelativePath", args);
        assertEquals(returns.length, 1);
        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getDetails().stringValue(),
                    "{\"message\":\"Can't make: " + targetPath + " relative to " + basePath + "\"}");
        } else {
            assertTrue(returns[0] instanceof BString);
            BString relative = (BString) returns[0];
            log.info("{ballerina/path}:relative(). Input: Base " + basePath + ", Target " + targetPath + " | Return: "
                    + relative.stringValue());
            assertEquals(relative.stringValue(), expected);
        }
    }

    @DataProvider(name = "filename_data")
    public Object[][] getFileNameDataset() {
        return new Object[][] {
            {"/A/B/C", "C", "C"}, 
            {"/foo/..", "..", ".."},
            {".", ".", "."},
            {"..", "..", ".."},
            {"../../", "..", ".."},
            {"foo/", "foo", "foo"},
            {"foo/bar/", "bar", "bar"},
            {"/AAA/////BBB/", "BBB", "BBB"},
            {"", "", ""},
            {"//////////////////", "", "error"},
            {"\\\\\\\\\\\\\\\\\\\\", "\\\\\\\\\\\\\\\\\\\\", "error"},
            {"/foo/./bar", "bar", "bar"},
            {"foo/../bar", "bar", "bar"},
            {"../foo/bar", "bar", "bar"},
            {"./foo/bar/../", "..", ".."},
            {"../../foo/../bar/zoo", "zoo", "zoo"},
            {"abc/../../././../def", "def", "def"},
            {"abc/def/../../..", "..", ".."},
            {"abc/def/../../../ghi/jkl/../../../mno", "mno", "mno"},
            // windows paths
            {"//server", "server", "error"},
            {"\\\\server", "\\\\server", "error"},
            {"C:/foo/..", "..", ".."},
            {"C:\\foo\\..", "C:\\foo\\..", ".."},
            {"D;\\bar\\baz", "D;\\bar\\baz", "baz"},
            {"bar\\baz", "bar\\baz", "baz"},
            {"bar/baz", "baz", "baz"},
            {"C:\\\\\\\\", "C:\\\\\\\\", ""},
            {"\\..\\A\\B", "\\..\\A\\B", "B"}
        };
    }

    @DataProvider(name = "is_absolute_data")
    public Object[][] isAbsoluteDataset() {
        return new Object[][] {
            {"/A/B/C", true, false}, 
            {"/foo/..", true, false},
            {".", false, false},
            {"..", false, false},
            {"../../", false, false},
            {"foo/", false, false},
            {"foo/bar/", false, false},
            {"/AAA/////BBB/", true, false},
            {"", false, false},
            {"//////////////////", true, false},
            {"\\\\\\\\\\\\\\\\\\\\", false, false},
            {"/foo/./bar", true, false},
            {"foo/../bar", false, false},
            {"../foo/bar", false, false},
            {"./foo/bar/../", false, false},
            {"../../foo/../bar/zoo", false, false},
            {"abc/../../././../def", false, false},
            {"abc/def/../../..", false, false},
            {"abc/def/../../../ghi/jkl/../../../mno", false, false},
            // windows paths
            {"//server", true, false},
            {"\\\\server", false, false},
            {"C:/foo/..", false, true},
            {"C:\\foo\\..", false, true},
            {"D;\\bar\\baz", false, false},
            {"bar\\baz", false, false},
            {"bar/baz", false, false},
            {"C:\\\\\\\\", false, true},
            {"\\..\\A\\B", false, false}
        };
    }

    @DataProvider(name = "parent_data")
    public Object[][] getParentDataset() {
        return new Object[][] {
                {"/A/B/C", "/A/B", "A\\B"},
                {"/foo/..", "/foo", "foo"},
                {".", "", ""},
                {"..", "", ""},
                {"../../", "..", ".."},
                {"foo/", "", ""},
                {"foo/bar/", "foo", "foo"},
                {"/AAA/////BBB/", "/AAA", "AAA"},
                {"", "", ""},
                {"//////////////////", "", "error"},
                {"\\\\\\\\\\\\\\\\\\\\", "", "error"},
                {"/foo/./bar", "/foo/.", "foo\\."},
                {"foo/../bar", "foo/..", "foo\\.."},
                {"../foo/bar", "../foo", "..\\foo"},
                {"./foo/bar/../", "./foo/bar", ".\\foo\\bar"},
                {"../../foo/../bar/zoo", "../../foo/../bar", "..\\..\\foo\\..\\bar"},
                {"abc/../../././../def", "abc/../../././..", "abc\\..\\..\\.\\.\\.."},
                {"abc/def/../../..", "abc/def/../..", "abc\\def\\..\\.."},
                {"abc/def/../../../ghi/jkl/../../../mno", "abc/def/../../../ghi/jkl/../../..",
                        "abc\\def\\..\\..\\..\\ghi\\jkl\\..\\..\\.."},
                // windows paths
                {"//server", "/", "error"},
                {"\\\\server", "", "error"},
                {"C:/foo/..", "C:/foo", "C:\\foo"},
                {"C:\\foo\\..", "", "C:\\foo"},
                {"D;\\bar\\baz", "", "D;\\bar"},
                {"bar\\baz", "", "bar"},
                {"bar/baz", "bar", "bar"},
                {"C:\\\\\\\\", "", "C:\\"},
                {"\\..\\A\\B", "", "..\\A"}
        };
    }

    @DataProvider(name = "windows_paths")
    public Object[] getWindowsPaths() {
        return new Object[] {
                "//server",
                "\\\\server",
                "C:/foo/..",
                "C:\\foo\\..",
                "D;\\bar\\baz",
                "bar\\baz",
                "bar/baz",
                ".",
                "C:\\\\\\\\",
                "\\..\\A\\B"
        };
    }

    @DataProvider(name = "file_parts")
    public Object[][] getFileParts() {
        return new Object[][] {
                {"", ""},
                {"/"},
                {"a"},
                {"A", "B", "C"},
                {"a", ""},
                {"", "b"},
                {"/", "a"},
                {"/", "a/b"},
                {"/", ""},
                {"//", "a"},
                {"/a", "b"},
                {"a/", "b"},
                {"a/", ""},
                {"/", "a", "b"},
                {"C:\\", "test", "data\\eat"}
        };
    }

    @DataProvider(name = "ext_parts")
    public Object[] getExtensionsSet() {
        return new Object[][] {
                {"path.bal", "bal"},
                {"path.pb.bal", "bal"},
                {"a.pb.bal/b", ""},
                {"a.toml/b.bal", "bal"},
                {"a.pb.bal/", "bal"},
                {"\\..\\A\\B.foo", "foo"},
                {"C:\\foo\\..\\bar", "\\bar"}
        };
    }

    @DataProvider(name = "relative_tests")
    public  Object[][] getRelativeSet() {
        return new Object[][] {
                {"a/b", "a/b", "."},
                {"a/b/.", "a/b", "."},
                {"a/b", "a/b/.", "."},
                {"./a/b", "a/b", "."},
                {"a/b", "./a/b", "."},
                {"ab/cd", "ab/cde", "../cde"},
                {"ab/cd", "ab/c", "../c"},
                {"a/b", "a/b/c/d", "c/d"},
                {"a/b", "a/b/../c", "../c"},
                {"a/b/../c", "a/b", "../b"},
                {"a/b/c", "a/c/d", "../../c/d"},
                {"a/b", "c/d", "../../c/d"},
                {"a/b/c/d", "a/b", "../.."},
                {"a/b/c/d", "a/b/", "../.."},
                {"a/b/c/d/", "a/b", "../.."},
                {"a/b/c/d/", "a/b/", "../.."},
                {"../../a/b", "../../a/b/c/d", "c/d"},
                {"/a/b", "/a/b", "."},
                {"/a/b/.", "/a/b", "."},
                {"/a/b", "/a/b/.", "."},
                {"/ab/cd", "/ab/cde", "../cde"},
                {"/ab/cd", "/ab/c", "../c"},
                {"/a/b", "/a/b/c/d", "c/d"},
                {"/a/b", "/a/b/../c", "../c"},
                {"/a/b/../c", "/a/b", "../b"},
                {"/a/b/c", "/a/c/d", "../../c/d"},
                {"/a/b", "/c/d", "../../c/d"},
                {"/a/b/c/d", "/a/b", "../.."},
                {"/a/b/c/d", "/a/b/", "../.."},
                {"/a/b/c/d/", "/a/b", "../.."},
                {"/a/b/c/d/", "/a/b/", "../.."},
                {"/../../a/b", "/../../a/b/c/d", "c/d"},
                {".", "a/b", "a/b"},
                {".", "..", ".."},

                {"..", ".", "error"},
                {"..", "a", "error"},
                {"../..", "..", "error"},
                {"a", "/a", "error"},
                {"/a", "a", "error"},
        };
    }
}
