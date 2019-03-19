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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
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

    @BeforeMethod
    public void setup() {
        fileOperationProgramFile = BCompileUtil.compile("test-src/path_test.bal");
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
        log.info("{ballerina/path}:isAbsolute(). Input: " + input + " is absolute: " + isAbs.booleanValue());
        assertEquals(isAbs.booleanValue(), Paths.get(input).isAbsolute());
    }


    @Test(description = "Test filename path function for posix paths", dataProvider = "posix_paths")
    public void testGetPosixFileName(String path) {
        validateFilename(path);
    }

    @Test(description = "Test filename path function for windows paths", dataProvider = "windows_paths")
    public void testGetWindowsFileName(String path) {
        validateFilename(path);
    }

    private void validateFilename(String input) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetFilename", args);
        BString filename = (BString) returns[0];
        log.info("{ballerina/path}:filename(). Input: " + input + " | Return: " + filename.stringValue());
        String expectedValue = Paths.get(input).getFileName() != null ? Paths.get(input).getFileName().toString() : "";
        assertEquals(filename.stringValue(), expectedValue);
    }

    @Test(description = "Test parent path function for posix paths", dataProvider = "posix_paths")
    public void testGetPosixParent(String path) {
        validateParent(path);
    }

    @Test(description = "Test parent path function for windows paths", dataProvider = "windows_paths")
    public void testGetWindowsParent(String path) {
        validateParent(path);
    }

    private void validateParent(String input) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetParent", args);
        BString filename = (BString) returns[0];
        log.info("{ballerina/path}:parent(). Input: " + input + " | Return: " + filename.stringValue());
        String expectedValue = Paths.get(input).getParent() != null ? Paths.get(input).getParent().toString() : "";
        assertEquals(filename.stringValue(), expectedValue);
    }

    @Test(description = "Test normalize path function for posix paths", dataProvider = "posix_paths")
    public void testPosixNormalizePath(String path) {
        validateNormalizePath(path);
    }

    @Test(description = "Test normalize path function for windows paths", dataProvider = "windows_paths")
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

    @Test(description = "Test split path function for posix paths", dataProvider = "posix_paths")
    public void testPosixSplitPath(String path) {
        validateSplitPath(path);
    }

    @Test(description = "Test split path function for windows paths", dataProvider = "windows_paths")
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

    @Test(description = "Test build path function for paths", dataProvider = "file_parts")
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

    @Test(description = "Test extension path function for posix paths", dataProvider = "ext_parts")
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

    @Test(description = "Test relative path function for posix paths", dataProvider = "relative_tests")
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

    @DataProvider(name = "posix_paths")
    public Object[] getPosixPaths() {
        return new Object[] {
                "/A/B/C",
                "/foo/..",
                ".",
                "..",
                "../../",
                "foo/",
                "foo/bar/",
                "/AAA/////BBB/",
                "",
                "//////////////////",
                "\\\\\\\\\\\\\\\\\\\\",
                "/foo/./bar",
                "foo/../bar",
                "../foo/bar",
                "./foo/bar/../",
                "../../foo/../bar/zoo",
                "abc/../../././../def",
                "abc/def/../../..",
                "abc/def/../../../ghi/jkl/../../../mno"
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
