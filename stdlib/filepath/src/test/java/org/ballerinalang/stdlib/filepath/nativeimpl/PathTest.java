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

package org.ballerinalang.stdlib.filepath.nativeimpl;

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.filepath.Constants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    private static final String TMPDIR_KEY = "java.io.tmpdir";
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
        log.info("{ballerina/filepath}:absolute(). Return value: " + absPath.stringValue());
        assertEquals(absPath.stringValue(), Paths.get(inputPath).toAbsolutePath().toString());
    }

    @Test(description = "Test absolute path function")
    public void testAbsolutePath() {
        String inputPath = "/test.txt";
        BValue[] args = {new BString(inputPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetAbsolutePath", args);
        BString absPath = (BString) returns[0];
        log.info("{ballerina/filepath}:absolute(). Return value: " + absPath.stringValue());
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
            assertEquals(error.getReason(), "{ballerina/filepath}InvalidPathError");
        } else {
            BString absPath = (BString) returns[0];
            log.info("{ballerina/filepath}:absolute(). Return value: " + absPath.stringValue());
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

    private void validateAbsolutePath(String input, boolean expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testIsAbsolutePath", args);
        assertTrue(returns[0] instanceof BBoolean);
        BBoolean isAbs = (BBoolean) returns[0];
        boolean actual = isAbs.booleanValue();
        log.info("{ballerina/filepath}:isAbsolute(). Input: " + input + " is absolute: " + actual);
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
            assertEquals(error.getReason(), "{ballerina/filepath}UNCPathError");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BString);
            BString filename = (BString) returns[0];
            log.info("{ballerina/filepath}:filename(). Input: " + path + " | Return: " + filename.stringValue());
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
            assertEquals(error.getReason(), "{ballerina/filepath}UNCPathError");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BString);
            BString parent = (BString) returns[0];
            log.info("{ballerina/filepath}:parent(). Input: " + input + " | Return: " + parent.stringValue());
            assertEquals(parent.stringValue(), expected, "Path: " + input + " filename()");
            String expectedValue = Paths.get(input).getParent() != null ?
                    Paths.get(input).getParent().toString() : "";
            assertEquals(parent.stringValue(), expectedValue, "Assert with Java | Path: " + input + " parent()");
        }
    }

    @Test(description = "Test normalize path function for paths", dataProvider = "normalize_data")
    public void testPosixNormalizePath(String path, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            validateNormalizePath(path, windowsOutput);
        } else {
            validateNormalizePath(path, posixOutput);
        }
    }

    private void validateNormalizePath(String input, String expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testNormalizePath", args);

        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/filepath}UNCPathError");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BString);
            BString filepath = (BString) returns[0];
            log.info("{ballerina/filepath}:normalize(). Input: " + input + " | Return: " + filepath.stringValue());
            assertEquals(filepath.stringValue(), expected, "Path: " + input + " normalize()");
            String expectedValue = Paths.get(input).normalize() != null ? Paths.get(input).normalize().toString() : "";
            assertEquals(filepath.stringValue(), expectedValue, "Assert with Java | Path: " + input + " normalize()");
        }
    }

    @Test(description = "Test split path function for paths", dataProvider = "split_data")
    public void testSplitPath(String path, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            validateSplitPath(path, windowsOutput);
        } else {
            validateSplitPath(path, posixOutput);
        }
    }

    private void validateSplitPath(String input, String expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testSplitPath", args);

        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/filepath}UNCPathError");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BValueArray);
            BValueArray parts = (BValueArray) returns[0];
            log.info("{ballerina/filepath}:split(). Input: " + input + " | Return: " + parts.stringValue());
            String[] expectedValues = expected.split(",");
            for (int i = 0; i < parts.size(); i++) {
                assertEquals(parts.getBValue(i).stringValue(), expectedValues[i], "Path: " + input + " normalize()");
                String expectedValue = Paths.get(input) != null ? Paths.get(input).getName(i).toString() : "";
                assertEquals(parts.getBValue(i).stringValue(), expectedValue, "Assert with Java | Path: " + input +
                        " normalize()");
            }

        }
    }

    @Test(description = "Test build path function for posix paths", dataProvider = "posix_file_parts")
    public void testPosixBuildPath(String[] parts, String expected) {
        if (!IS_WINDOWS) {
            validateBuildPath(parts, expected);
        }
    }

    @Test(description = "Test build path function for windows paths", dataProvider = "windows_file_parts")
    public void testBuildPath(String[] parts, String expected) {
        if (IS_WINDOWS) {
            validateBuildPath(parts, expected);
        }
    }

    private void validateBuildPath(String[] parts, String expected) {
        BValueArray valueArray = new BValueArray(BTypes.typeString);
        int i = 0;
        for (String part : parts) {
            valueArray.add(i++, part);
        }
        BValue[] args = {valueArray};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testBuildPath", args);
        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), "{ballerina/filepath}UNCPathError");
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BString);
            BString resultPath = (BString) returns[0];
            log.info("{ballerina/filepath}:build(). Input: " + Arrays.asList(parts) + " | Return: " + resultPath);
            assertEquals(resultPath.stringValue(), expected);
        }
    }

    @Test(description = "Test extension path function for paths", dataProvider = "ext_parts")
    public void testPathExtension(String path, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            validateFileExtension(path, windowsOutput);
        } else {
            validateFileExtension(path, posixOutput);
        }
    }

    private void validateFileExtension(String input, String expected) {
        BValue[] args = {new BString(input)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testPathExtension", args);
        BString extension = (BString) returns[0];
        log.info("{ballerina/filepath}:extension(). Input: " + input + " | Return: " + extension.stringValue());
        assertEquals(extension.stringValue(), expected);
    }

    @Test(description = "Test relative path function for posix paths", dataProvider = "relative_tests")
    public void testRelativePath(String basePath, String targetPath, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            validateRelativePath(basePath, targetPath, windowsOutput);
        } else {
            validateRelativePath(basePath, targetPath, posixOutput);
        }

    }

    private void validateRelativePath(String basePath, String targetPath, String expected) {
        BValue[] args = {new BString(basePath), new BString(targetPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testRelativePath", args);
        assertEquals(returns.length, 1);
        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getDetails().stringValue(),
                         "{message:\"Can't make: " + targetPath + " relative to " + basePath + "\"}");
        } else {
            assertTrue(returns[0] instanceof BString);
            BString relative = (BString) returns[0];
            log.info("{ballerina/filepath}:relative(). Input: Base " + basePath + ", Target " + targetPath + " | " +
                             "Return: " + relative.stringValue());
            assertEquals(relative.stringValue(), expected);
        }
    }

    @Test(description = "Test resolve path function for paths")
    public void testResolvePath() throws IOException {
        if (IS_WINDOWS) {
            // Temporary disable running this test in windows, due to permission failure in symbolic link creation.
            return;
        }
        Path symLinkPath = null;
        try {
            Path filePath = Paths.get("src", "test", "resources", "data-files", "test.txt");
            symLinkPath = Paths.get(System.getProperty(TMPDIR_KEY), "test_link.txt");
            Files.deleteIfExists(symLinkPath);
            Files.createSymbolicLink(symLinkPath, filePath);

            BValue[] args = {new BString(symLinkPath.toString())};
            BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testResolvePath", args);
            assertEquals(returns.length, 1);
            assertTrue(returns[0] instanceof BString);
            BString resolvePath = (BString) returns[0];

            log.info("{ballerina/filepath}:resolve(). Return value: " + resolvePath.stringValue());
            assertEquals(resolvePath.stringValue(), Files.readSymbolicLink(symLinkPath).toString());
        } catch (IOException e) {
            Assert.fail("Error while creating symbolic link", e);
        } finally {
            if (symLinkPath != null) {
                Files.deleteIfExists(symLinkPath);
            }
        }
    }

    @Test(description = "Test resolve path function for not link path")
    public void testResolveNotLinkPath() {
        Path filePath = Paths.get("src", "test", "resources", "data-files", "test_nolink.txt");
        BValue[] args = {new BString(filePath.toString())};

        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testResolvePath", args);
        assertEquals(returns.length, 1);
        assertTrue(returns[0] instanceof BError);
        BError resolveError = (BError) returns[0];
        assertEquals(resolveError.getReason(), Constants.NOT_LINK_ERROR);
        log.info("Ballerina error: " + resolveError.getDetails().stringValue());
    }

    @Test(description = "Test resolve path function for non existence path")
    public void testResolveNonExistencePath() {
        Path filePath = Paths.get("src", "test", "resources", "data-files", "test_nonexist.txt");
        BValue[] args = {new BString(filePath.toString())};

        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testResolvePath", args);
        assertEquals(returns.length, 1);
        assertTrue(returns[0] instanceof BError);
        BError resolveError = (BError) returns[0];
        assertEquals(resolveError.getReason(), Constants.FILE_NOT_FOUND_ERROR);
        log.info("Ballerina error: " + resolveError.getDetails().stringValue());
    }

    @Test(description = "Test path matches", dataProvider = "match_test")
    public void testPathMatch(String pattern, String path, String posixOutput, String windowsOutput) {
        if (IS_WINDOWS) {
            testPathMatch(pattern, path, windowsOutput);
        } else {
            testPathMatch(pattern, path, posixOutput);
        }
    }

    private void testPathMatch(String pattern, String path, String expected) {
        BValue[] args = {new BString(path), new BString(pattern)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testPathMatches", args);

        if ("error".equals(expected)) {
            assertTrue(returns[0] instanceof BError);
            BError error = (BError) returns[0];
            assertEquals(error.getReason(), Constants.INVALID_PATTERN_ERROR);
            log.info("Ballerina error: " + error.getDetails().stringValue());
        } else {
            assertTrue(returns[0] instanceof BBoolean);
            BBoolean matches = (BBoolean) returns[0];
            log.info("{ballerina/filepath}:matches(). Input: " + path + " | Return: " + matches.booleanValue());
            assertEquals(matches.booleanValue(), Boolean.parseBoolean(expected), "Path: " + path + " matches() with " +
                    "pattern" + pattern);
        }
    }

    @DataProvider(name = "filename_data")
    public Object[][] getFileNameDataset() {
        return new Object[][]{
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
                {"\\..\\A\\B", "\\..\\A\\B", "B"},
                {"c:\\test.txt", "c:\\test.txt", "test.txt"}
        };
    }

    @DataProvider(name = "is_absolute_data")
    public Object[][] isAbsoluteDataset() {
        return new Object[][]{
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
                {"\\\\host\\share\\foo", false, true},
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
        return new Object[][]{
                {"/A/B/C", "/A/B", "\\A\\B"},
                {"/foo/..", "/foo", "\\foo"},
                {".", "", ""},
                {"..", "", ""},
                {"../../", "..", ".."},
                {"foo/", "", ""},
                {"foo/bar/", "foo", "foo"},
                {"/AAA/////BBB/", "/AAA", "\\AAA"},
                {"", "", ""},
                {"//////////////////", "", "error"},
                {"\\\\\\\\\\\\\\\\\\\\", "", "error"},
                {"/foo/./bar", "/foo/.", "\\foo\\."},
                {"foo/../bar", "foo/..", "foo\\.."},
                {"../foo/bar", "../foo", "..\\foo"},
                {"./foo/bar/../", "./foo/bar", ".\\foo\\bar"},
                {"../../foo/../bar/zoo", "../../foo/../bar", "..\\..\\foo\\..\\bar"},
                {"abc/../../././../def", "abc/../../././..", "abc\\..\\..\\.\\.\\.."},
                {"abc/def/../../..", "abc/def/../..", "abc\\def\\..\\.."},
                {"abc/def/../../../ghi/jkl/../../../mno", "abc/def/../../../ghi/jkl/../../..",
                        "abc\\def\\..\\..\\..\\ghi\\jkl\\..\\..\\.."},
                {"/", "", ""},
                {"/A", "/", "\\"},
                // windows paths
                {"//server", "/", "error"},
                {"\\\\server", "", "error"},
                {"C:/foo/..", "C:/foo", "C:\\foo"},
                {"C:\\foo\\..", "", "C:\\foo"},
                {"D;\\bar\\baz", "", "D;\\bar"},
                {"bar\\baz", "", "bar"},
                {"bar/baz", "bar", "bar"},
                {"C:\\\\\\\\", "", ""},
                {"\\..\\A\\B", "", "\\..\\A"}
        };
    }

    @DataProvider(name = "normalize_data")
    public Object[][] getNormalizedDataset() {
        return new Object[][]{
                {"/A/B/C", "/A/B/C", "\\A\\B\\C"},
                {"/foo/..", "/", "\\"},
                {".", "", ""},
                {"..", "..", ".."},
                {"../../", "../..", "..\\.."},
                {"foo/", "foo", "foo"},
                {"foo/bar/", "foo/bar", "foo\\bar"},
                {"/AAA/////BBB/", "/AAA/BBB", "\\AAA\\BBB"},
                {"", "", ""},
                {"//////////////////", "/", "error"},
                {"\\\\\\\\\\\\\\\\\\\\", "\\\\\\\\\\\\\\\\\\\\", "error"},
                {"/foo/./bar", "/foo/bar", "\\foo\\bar"},
                {"foo/../bar", "bar", "bar"},
                {"../foo/bar", "../foo/bar", "..\\foo\\bar"},
                {"./foo/bar/../", "foo", "foo"},
                {"../../foo/../bar/zoo", "../../bar/zoo", "..\\..\\bar\\zoo"},
                {"abc/../../././../def", "../../def", "..\\..\\def"},
                {"abc/def/../../..", "..", ".."},
                {"abc/def/../../../ghi/jkl/../../../mno", "../../mno",
                        "..\\..\\mno"},
                {"/", "/", "\\"},
                {"/A", "/A", "\\A"},
                {"/../A/B", "/A/B", "\\A\\B"},
                // windows paths
                {"//server", "/server", "error"},
                {"\\\\server", "\\\\server", "error"},
                {"C:/foo/..", "C:", "C:\\"},
                {"C:\\foo\\..", "C:\\foo\\..", "C:\\"},
                {"C:\\..\\foo", "C:\\..\\foo", "C:\\foo"},
                {"D;\\bar\\baz", "D;\\bar\\baz", "D;\\bar\\baz"},
                {"bar\\baz", "bar\\baz", "bar\\baz"},
                {"bar/baz", "bar/baz", "bar\\baz"},
                {"C:\\\\\\\\", "C:\\\\\\\\", "C:\\"},
                {"\\..\\A\\B", "\\..\\A\\B", "\\A\\B"}
        };
    }

    @DataProvider(name = "split_data")
    public Object[][] getSplitDataset() {
        return new Object[][]{
                {"/A/B/C", "A,B,C", "A,B,C"},
                {"/foo/..", "foo,..", "foo,.."},
                {".", ".", "."},
                {"..", "..", ".."},
                {"../../", "..,..", "..,.."},
                {"foo/", "foo", "foo"},
                {"foo/bar/", "foo,bar", "foo,bar"},
                {"/AAA/////BBB/", "AAA,BBB", "AAA,BBB"},
                {"", "", ""},
                {"//////////////////", "/", "error"},
                {"\\\\\\\\\\\\\\\\\\\\", "\\\\\\\\\\\\\\\\\\\\", "error"},
                {"/foo/./bar", "foo,.,bar", "foo,.,bar"},
                {"foo/../bar", "foo,..,bar", "foo,..,bar"},
                {"../foo/bar", "..,foo,bar", "..,foo,bar"},
                {"./foo/bar/../", ".,foo,bar,..", ".,foo,bar,.."},
                {"../../foo/../bar/zoo", "..,..,foo,..,bar,zoo", "..,..,foo,..,bar,zoo"},
                {"abc/../../././../def", "abc,..,..,.,.,..,def", "abc,..,..,.,.,..,def"},
                {"abc/def/../../..", "abc,def,..,..,..", "abc,def,..,..,.."},
                {"abc/def/../../../ghi/jkl/../../../mno", "abc,def,..,..,..,ghi,jkl,..,..,..,mno",
                        "abc,def,..,..,..,ghi,jkl,..,..,..,mno"},
                {"/", "/", "\\"},
                {"/A", "A", "A"},
                {"/../A/B", "..,A,B", "..,A,B"},
                // windows paths
                {"//server", "server", "error"},
                {"\\\\server", "\\\\server", "error"},
                {"C:/foo/..", "C:,foo,..", "foo,.."},
                {"C:\\foo\\..", "C:\\foo\\..", "foo,.."},
                {"C:\\..\\foo", "C:\\..\\foo", "..,foo"},
                {"D;\\bar\\baz", "D;\\bar\\baz", "D;,bar,baz"},
                {"bar\\baz", "bar\\baz", "bar,baz"},
                {"bar/baz", "bar,baz", "bar,baz"},
                {"C:\\\\\\\\", "C:\\\\\\\\", "C:\\"},
                {"\\..\\A\\B", "\\..\\A\\B", "..,A,B"}
        };
    }

    @DataProvider(name = "posix_file_parts")
    public Object[][] getPosixFileParts() {
        return new Object[][]{
                {new String[]{}, ""},
                {new String[]{""}, ""},
                {new String[]{"/"}, "/"},
                {new String[]{"a"}, "a"},
                {new String[]{"A", "B", "C"}, "A/B/C"},
                {new String[]{"a", ""}, "a"},
                {new String[]{"", "b"}, "b"},
                {new String[]{"/", "a"}, "/a"},
                {new String[]{"/", "a/b"}, "/a/b"},
                {new String[]{"/", ""}, "/"},
                {new String[]{"//", "a"}, "/a"},
                {new String[]{"/a", "b"}, "/a/b"},
                {new String[]{"a/", "b"}, "a/b"},
                {new String[]{"a/", ""}, "a"},
                {new String[]{"", ""}, ""},
                {new String[]{"/", "a", "b"}, "/a/b"},
                {new String[]{"C:\\", "test", "data\\eat"}, "C:\\/test/data\\eat"},
                {new String[]{"C:", "test", "data\\eat"}, "C:/test/data\\eat"}
        };
    }

    @DataProvider(name = "windows_file_parts")
    public Object[][] getWindowsFileParts() {
        return new Object[][]{
                {new String[]{"directory", "file"}, "directory\\file"},
                {new String[]{"C:\\Windows\\", "System32"}, "C:\\Windows\\System32"},
                {new String[]{"C:\\Windows\\", ""}, "C:\\Windows"},
                {new String[]{"C:\\", "Windows"}, "C:\\Windows"},
                {new String[]{"C:", "a"}, "C:a"},
                {new String[]{"C:", "a\\b"}, "C:a\\b"},
                {new String[]{"C:", "a", "b"}, "C:a\\b"},
                {new String[]{"C:", "", "b"}, "C:b"},
                {new String[]{"C:", "", "", "b"}, "C:b"},
                {new String[]{"C:", ""}, "C:"},
                {new String[]{"C:", "", ""}, "C:"},
                {new String[]{"C:.", "a"}, "C:\\a"},
                {new String[]{"C:a", "b"}, "C:a\\b"},
                {new String[]{"C:a", "b", "d"}, "C:a\\b\\d"},
                {new String[]{"\\\\host\\share", "foo"}, "\\\\host\\share\\foo"},
                {new String[]{"\\\\host\\share\\foo"}, "\\\\host\\share\\foo"},
                {new String[]{"//host/share", "foo/bar"}, "\\\\host\\share\\foo\\bar"},
                {new String[]{"\\"}, "\\"},
                {new String[]{"\\", ""}, "\\"},
                {new String[]{"\\", "a"}, "\\a"},
                {new String[]{"\\", "a", "b"}, "\\a\\b"},
                {new String[]{"\\", "\\\\a\\b", "c"}, "\\a\\b\\c"},
                {new String[]{"\\\\a", "b", "c"}, "error"},
                {new String[]{"\\\\a\\", "b", "c"}, "error"},
        };
    }

    @DataProvider(name = "ext_parts")
    public Object[] getExtensionsSet() {
        return new Object[][]{
                {"path.bal", "bal", "bal"},
                {"path.pb.bal", "bal", "bal"},
                {"a.pb.bal/b", "", ""},
                {"a.toml/b.bal", "bal", "bal"},
                {"a.pb.bal/", "", ""},
                {"\\..\\A\\B.foo", "foo", "foo"},
                {"C:\\foo\\..\\bar", "\\bar", ""}
        };
    }

    @DataProvider(name = "relative_tests")
    public Object[][] getRelativeSet() {
        return new Object[][]{
                {"a/b", "a/b", ".", "."},
                {"a/b/.", "a/b", ".", "."},
                {"a/b", "a/b/.", ".", "."},
                {"./a/b", "a/b", ".", "."},
                {"a/b", "./a/b", ".", "."},
                {"ab/cd", "ab/cde", "../cde", "..\\cde"},
                {"ab/cd", "ab/c", "../c", "..\\c"},
                {"a/b", "a/b/c/d", "c/d", "c\\d"},
                {"a/b", "a/b/../c", "../c", "..\\c"},
                {"a/b/../c", "a/b", "../b", "..\\b"},
                {"a/b/c", "a/c/d", "../../c/d", "..\\..\\c\\d"},
                {"a/b", "c/d", "../../c/d", "..\\..\\c\\d"},
                {"a/b/c/d", "a/b", "../..", "..\\.."},
                {"a/b/c/d", "a/b/", "../..", "..\\.."},
                {"a/b/c/d/", "a/b", "../..", "..\\.."},
                {"a/b/c/d/", "a/b/", "../..", "..\\.."},
                {"../../a/b", "../../a/b/c/d", "c/d", "c\\d"},
                {"/a/b", "/a/b", ".", "."},
                {"/a/b/.", "/a/b", ".", "."},
                {"/a/b", "/a/b/.", ".", "."},
                {"/ab/cd", "/ab/cde", "../cde", "..\\cde"},
                {"/ab/cd", "/ab/c", "../c", "..\\c"},
                {"/a/b", "/a/b/c/d", "c/d", "c\\d"},
                {"/a/b", "/a/b/../c", "../c", "..\\c"},
                {"/a/b/../c", "/a/b", "../b", "..\\b"},
                {"/a/b/c", "/a/c/d", "../../c/d", "..\\..\\c\\d"},
                {"/a/b", "/c/d", "../../c/d", "..\\..\\c\\d"},
                {"/a/b/c/d", "/a/b", "../..", "..\\.."},
                {"/a/b/c/d", "/a/b/", "../..", "..\\.."},
                {"/a/b/c/d/", "/a/b", "../..", "..\\.."},
                {"/a/b/c/d/", "/a/b/", "../..", "..\\.."},
                {"/../../a/b", "/../../a/b/c/d", "c/d", "c\\d"},
                {".", "a/b", "a/b", "a\\b"},
                {".", "..", "..", ".."},

                {"..", ".", "error", "error"},
                {"..", "a", "error", "error"},
                {"../..", "..", "error", "error"},
                {"a", "/a", "error", "error"},
                {"/a", "a", "error", "error"},

                {"C:a\\b\\c", "C:a/b/d", "../C:a/b/d", "..\\d"},
                {"C:\\", "D:\\", "../D:\\", "error"},
                {"C:", "D:", "../D:", "error"},
                {"C:\\Projects", "c:\\projects\\src", "../c:\\projects\\src", "src"},
                {"C:\\Projects", "c:\\projects", "../c:\\projects", "."},
                {"C:\\Projects\\a\\..", "c:\\projects", "../c:\\projects", "."},
        };
    }

    @DataProvider(name = "match_test")
    public Object[] getMatchesSet() {
        return new Object[][]{
                {"abc", "abc", "true", "true"},
                {"*", "abc", "true", "true"},
                {"*c", "abc", "true", "true"},
                {"a*", "a", "true", "true"},
                {"a*", "abc", "true", "true"},
                {"a*", "ab/c", "false", "false"},
                {"a*/b", "abc/b", "true", "true"},
                {"a*/b", "a/c/b", "false", "false"},
                {"A*B*C*D*E*/f", "AxBxCxDxE/f", "true", "true"},
                {"a*b*c*d*e*/f", "axbxcxdxexxx/f", "true", "true"},
                {"a*b*c*d*e*/f", "axbxcxdxe/xxx/f", "false", "false"},
                {"a*b*c*d*e*/f", "axbxcxdxexxx/fff", "false", "false"},
                {"a*b?c*x", "abxbbxdbxebxczzx", "true", "true"},
                {"a*b?c*x", "abxbbxdbxebxczzy", "false", "false"},
                {"ab[c]", "abc", "true", "true"},
                {"ab[b-d]", "abc", "true", "true"},
                {"ab[e-g]", "abc", "false", "false"},
                {"[a-b-c]", "a", "error", "error"},
                {"[", "a", "error", "error"},
                {"a[", "a", "error", "error"},
                {"[-]", "-", "true", "true"},
                {"[x-]", "x", "true", "true"},
                {"[]a]", "a", "error", "error"},
                {"[\\-x]", "x", "true", "error"},
                {"a?b", "a/b", "false", "false"},
                {"a*b", "a/b", "false", "false"},
                {"[\\-]", "-", "true", "error"},
                {"[x\\-]", "x", "true", "error"},
                {"[x\\-]", "-", "true", "error"},
                {"[x\\-]", "z", "false", "error"},
                {"[\\-x]", "x", "true", "error"},
                {"[\\-x]", "z", "false", "error"},
                {"[\\-x]", "-", "false", "error"},
                {"[\\-x]", "a", "true", "error"}
        };
    }
}
