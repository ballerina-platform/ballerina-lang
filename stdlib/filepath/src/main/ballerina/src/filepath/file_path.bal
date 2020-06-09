// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/log;
import ballerina/system;
import ballerina/stringutils;
import ballerina/java;

boolean isWindows = system:getEnv("OS") != "";
string pathSeparator = isWindows ? "\\" : "/";
string pathListSeparator = isWindows ? ";" : ":";

# Retrieves the absolute path from the provided location.
# ```ballerina
#  string|filepath:Error absolutePath = filepath:absolute(<@untainted> "test.txt");
# ```
#
# + path - String value of the file path free from potential malicious codes
# + return - The absolute path reference or else a `filepath:Error` if the path cannot be derived
public function absolute(@untainted string path) returns string|Error {
    var result = externAbsolute(java:fromString(path));
    if (result is Error) {
        return result;
    } else {
        var stringResult = java:toString(result);
        if (stringResult is string) {
            return stringResult;
        } else {
            Error e = prepareError(GENERIC_ERROR, "Error occurred while retrieving the absolute path.");
            return e;
        }
    }
}

function externAbsolute(handle path) returns handle|Error =
@java:Method {
    name: "absolute",
    class: "org.ballerinalang.stdlib.filepath.nativeimpl.FilePathUtils"
} external;

# Returns the path separator of the underlying operating system.
# ```ballerina
#  string pathSeparator = filepath:getPathSeparator();
# ```
#
# + return - String value of the path separator
public function getPathSeparator() returns string {
    return pathSeparator;
}

# Returns the path variable's separating character for paths of the underlying operating system.
# ```ballerina
#  string pathListSeparator = filepath:getPathListSeparator();
# ```
#
# + return - String value of the path list separator
public function getPathListSeparator() returns string {
    return pathListSeparator;
}

# Reports whether the path is absolute.
# A path is absolute if it is independent of the current directory.
# On Unix, a path is absolute if it starts with the root.
# On Windows, a path is absolute if it has a prefix and starts with the root: c:\windows.
# ```ballerina
#  boolean|filepath:Error isAbsolute = filepath:isAbsolute("/A/B/C");
# ```
#
# + path - String value of the file path
# + return - `true` if path is absolute, `false` otherwise, or else an `filepath:Error`
#            occurred if the path is invalid
public function isAbsolute(string path) returns boolean|Error {
    if (path.length() <= 0) {
        return false;
    }
    if (isWindows) {
        return check getVolumnNameLength(path) > 0;
    } else {
        return check charAt(path, 0) == "/";
    }
}

# Retrieves the base name of the file from the provided location,
# which is the last element of the path.
# Trailing path separators are removed before extracting the last element.
# ```ballerina
#  string|filepath:Error name = filepath:filename("/A/B/C.txt");
# ```
#
# + path - String value of file path
# + return - The name of the file or else a `filepath:Error` if the path is invalid
public function filename(string path) returns string|Error {
    string validatedPath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(validatedPath);
    int count = offsetIndexes.length();
    if (count == 0) {
        return "";
    }
    if (count == 1 && validatedPath.length() > 0) {
        if !(check isAbsolute(validatedPath)) {
            return validatedPath;
        }
    }
    int lastOffset = offsetIndexes[count - 1];
    return validatedPath.substring(lastOffset, validatedPath.length());
}

# Returns the enclosing parent directory.
# If the path is empty, parent returns ".".
# The returned path does not end in a separator unless it is the root directory.
# ```ballerina
#  string|filepath:Error parentPath = filepath:parent("/A/B/C.txt");
# ```
#
# + path - String value of the file/directory path
# + return - Path of the parent directory or else a `filepath:Error`
#            if an error occurred while getting the parent directory
public function parent(string path) returns string|Error {
    string validatedPath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(validatedPath);
    int count = offsetIndexes.length();
    if (count == 0) {
        return "";
    }
    int len = offsetIndexes[count-1] - 1;
    if (len < 0) {
        return "";
    }
    int offset;
    string root;
    [root, offset] = check getRoot(validatedPath);
    if (len < offset) {
        return root;
    }
    return validatedPath.substring(0, len);
}

# Returns the shortest path name equivalent to the given path.
# Replace the multiple separator elements with a single one.
# Eliminate each "." path name element (the current directory).
# Eliminate each inner ".." path name element (the parent directory).
# ```ballerina
#  string|filepath:Error normalizedPath = filepath:normalize("foo/../bar");
# ```
#
# + path - String value of the file path
# + return - Normalized file path or else a `filepath:Error` if the path is invalid
public function normalize(string path) returns string|Error {
    string validatedPath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(validatedPath);
    int count = offsetIndexes.length();
    if (count == 0 || isEmpty(validatedPath)) {
        return validatedPath;
    }

    string root;
    int offset;
    [root, offset] = check getRoot(validatedPath);
    string c0 = check charAt(path, 0);

    int i = 0;
    string[] parts = [];
    boolean[] ignore = [];
    boolean[] parentRef = [];
    int remaining = count;
    while(i < count) {
        int begin = offsetIndexes[i];
        int length;
        ignore[i] = false;
        parentRef[i] = false;
        if (i == (count - 1)) {
            length = validatedPath.length() - begin;
            parts[i] = validatedPath.substring(begin, validatedPath.length());
        } else {
            length = offsetIndexes[i + 1] - begin - 1;
            parts[i] = validatedPath.substring(begin, offsetIndexes[i + 1] - 1);
        }
        if (check charAt(validatedPath, begin) == ".") {
            if (length == 1) {
                ignore[i] = true;
                remaining = remaining - 1;
            } else if (length == 2 && check charAt(validatedPath, begin + 1) == ".") {
                parentRef[i] = true;
                int j = i - 1;
                boolean hasPrevious = false;
                while (j >= 0) {
                    // A/B/<ignore>/..
                    if (ignore.length() > 0 && !parentRef[j] && !ignore[j]) {
                        ignore[j] = true;
                        remaining = remaining - 1;
                        hasPrevious = true;
                        break;
                    }
                    j = j - 1;
                }
                if (hasPrevious || (offset > 0) || isSlash(c0)) {
                    ignore[i] = true;
                    remaining = remaining - 1;
                }
            }
        }
        i = i + 1;
    }

    if (remaining == count) {
        return validatedPath;
    }

    if (remaining == 0) {
        return root;
    }

    string normalizedPath = "";
    if (root != "") {
        normalizedPath = normalizedPath + root;
    }
    i = 0;
    while (i < count) {
        if (!ignore[i] && (offset <= offsetIndexes[i])) {
            normalizedPath = normalizedPath + parts[i] + pathSeparator;
        }
        i = i + 1;
    }
    return parse(normalizedPath);
}

# Splits a list of paths joined by the OS-specific path separator.
# ```ballerina
#  string[]|filepath:Error parts = filepath:split("/A/B/C");
# ```
#
# + path - String value of the file path
# + return - String array of the part components or else a `filepath:Error` if the path is invalid
public function split(string path) returns string[]|Error {
    string validatedPath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(validatedPath);
    int count = offsetIndexes.length();

    string[] parts = [];
    int i = 0;
    while (i < count) {
        int begin = offsetIndexes[i];
        int length;
        if (i == (count - 1)) {
            length = validatedPath.length() - begin;
            parts[i] = check parse(validatedPath.substring(begin, validatedPath.length()));
        } else {
            length = offsetIndexes[i + 1] - begin - 1;
            parts[i] = check parse(validatedPath.substring(begin, offsetIndexes[i + 1] - 1));
        }
        i = i + 1;
    }
    return parts;
}

# Joins any number of path elements into a single path.
# ```ballerina
#  string|filepath:Error path = filepath:build("/", "foo", "bar");
# ```
#
# + parts - String values of the file path parts
# + return - String value of the file path or else a `filepath:Error` if the parts are invalid
public function build(string... parts) returns string|Error {
    if (isWindows) {
        return check buildWindowsPath(...parts);
    } else {
        return check buildUnixPath(...parts);
    }
}

# Reports whether the filename is reserved.
# Reserved words only exist in Windows.
# ```ballerina
#  boolean|filepath:Error path = filepath:isReservedName("abc.txt");
# ```
#
# + name - Filename
# + return - True if the path is a Windows reserved name or else false otherwise
public function isReservedName(string name) returns boolean {
    if (isWindows) {
        return isWindowsReservedName(name);
    }
    // unix system doesn't have any reserved names.
    return false;
}

# Retrieves the extension of the file path.
# The extension is the suffix beginning at the final dot in the final element of the path.
# It is empty if there is no dot.
# ```ballerina
#  string|filepath:Error extension = filepath:extension("path.bal");
# ```
#
# + path - String value of the file path
# + return - The extension of the file, an empty string if there is no extension,
#            or else a `filepath:Error` if the path is invalid
public function extension(string path) returns string|Error {
    if (path.endsWith(pathSeparator) || (isWindows && path.endsWith("/"))) {
      return  "";
    }
    string validatedPath = check parse(path);
    int count = validatedPath.length();
    if (count == 0) {
        return validatedPath;
    }
    int i = count - 1;
    while (i >= 0) {
        string char = check charAt(validatedPath, i);
        if (char == pathSeparator) {
            break;
        }
        if (char == ".") {
            return validatedPath.substring(i + 1, count);
        }
        i = i - 1;
    }
    return "";
}

# Returns a relative path, which is logically equivalent to the target path when joined to the base path with an
# intervening separator.
# An error is returned if the target path cannot be made relative to the base path.
# ```ballerina
#  string|filepath:Error relativePath = filepath:relative("a/b/c", "a/c/d");
# ```
#
# + base - String value of the base file path
# + target - String value of the target file path
# + return - The extension of the file, empty string otherwise, or else an
#            `filepath:Error` occurred if at least one path is invalid
public function relative(string base, string target) returns string|Error {
    string cleanBase = check normalize(base);
    string cleanTarget = check normalize(target);
    if (isSamePath(cleanBase, cleanTarget)) {
        return ".";
    }
    string baseRoot;
    int baseOffset;
    [baseRoot, baseOffset] = check getRoot(cleanBase);
    string targetRoot;
    int targetOffset;
    [targetRoot, targetOffset] = check getRoot(cleanTarget);
    if (!isSamePath(baseRoot, targetRoot)) {
        return RelativePathError("Can't make: " + target + " relative to " + base);
    }
    int b0 = baseOffset;
    int bi = baseOffset;
    int t0 = targetOffset;
    int ti = targetOffset;
    int bl = cleanBase.length();
    int tl = cleanTarget.length();
    while (true) {
        while (bi < bl && !isSlash(check charAt(cleanBase, bi))) {
            bi = bi + 1;
        }
        while (ti < tl && !isSlash(check charAt(cleanTarget, ti))) {
            ti = ti + 1;
        }
        if (!isSamePath(cleanBase.substring(b0, bi), cleanTarget.substring(t0, ti))) {
            break;
        }
        if (bi < bl) {
           bi = bi + 1;
        }
        if (ti < tl) {
            ti = ti + 1;
        }
        b0 = bi;
        t0 = ti;
    }
    if (cleanBase.substring(b0, bi) == "..") {
        return RelativePathError("Can't make: " + target + " relative to " + base);
    }
    if (b0 != bl) {
        string remainder = cleanBase.substring(b0, bl);
        int[] offsets = check getOffsetIndexes(remainder);
        int noSeparators = offsets.length() - 1;
        string relativePath = "..";
        int i = 0;
        while (i < noSeparators) {
            relativePath = relativePath + pathSeparator + "..";
            i = i + 1;
        }
        if (t0 != tl) {
            relativePath = relativePath + pathSeparator + cleanTarget.substring(t0, tl);
        }
        return relativePath;
    }
    return cleanTarget.substring(t0, tl);
}

# Returns the filepath after the evaluation of any symbolic links.
# If the path is relative, the result will be relative to the current directory
# unless one of the components is an absolute symbolic link.
# Resolves normalising the calls on the result.
# ```ballerina
#  string|filepath:Error resolvedPath = filepath:resolve("a/b/c");
# ```
#
# + path - Security-validated string value of the file path
# + return - Resolved file path or else a `filepath:Error` if the path is invalid
public function resolve(@untainted string path) returns string|Error = @java:Method {
    name: "resolve",
    class: "org.ballerinalang.stdlib.filepath.nativeimpl.FilePathUtils"
} external;

# Reports whether the complete filename (not just a substring of it) matches the provided Glob pattern.
# An error is returned if the pattern is malformed.
# ```ballerina
#  boolean|filepath:Error matches = filepath:matches("a/b/c.java", "glob:*.{java,class}");
# ```
#
# + path - String value of the file path
# + pattern - String value of the target file path
# + return - `true` if the filename of the path matches with the pattern, `false` otherwise,
#            or else an `filepath:Error` if the path or pattern is invalid
public function matches(string path, string pattern) returns boolean|Error = @java:Method {
    name: "matches",
    class: "org.ballerinalang.stdlib.filepath.nativeimpl.FilePathUtils"
} external;

# Parses the give path and remove redundent slashes.
#
# + input - String path value
# + return - Parsed path or else a `filepath:Error` if the given path is invalid
function parse(string input) returns string|Error {
    if (input.length() <= 0) {
        return input;
    }
    if (isWindows) {
        int offset = 0;
        string root = "";
        [root, offset] = check getRoot(input);
        return root + check parseWindowsPath(input, offset);
    } else {
        int n = input.length();
        string prevC = "";
        int i = 0;
        while (i < n) {
            string c = check charAt(input, i);
            if ((c == "/") && (prevC == "/")) {
                return parsePosixPath(input, i - 1);
            }
            prevC = c;
            i = i + 1;
        }
        if (prevC == "/") {
            return parsePosixPath(input, n - 1);
        }
        return input;
    }
}

function getRoot(string input) returns [string,int]|Error {
    if (isWindows) {
        return getWindowsRoot(input);
    } else {
        return getUnixRoot(input);
    }
}

function isSlash(string c) returns boolean {
    if (isWindows) {
        return isWindowsSlash(c);
    } else {
        return isPosixSlash(c);
    }
}

function nextNonSlashIndex(string path, int offset, int end) returns int|Error {
    int off = offset;
    while(off < end && isSlash(check charAt(path, off))) {
        off = off + 1;
    }
    return off;
}

function nextSlashIndex(string path, int offset, int end) returns int|Error {
    int off = offset;
    while(off < end && !isSlash(check charAt(path, off))) {
        off = off + 1;
    }
    return off;
}

function isLetter(string c) returns boolean {
    string regEx = "^[a-zA-Z]{1}$";
    boolean|error letter = stringutils:matches(c,regEx);
    if (letter is error) {
        log:printError("Error while checking input character is string", letter);
        return false;
    } else {
        return letter;
    }
}

function isUNC(string path) returns boolean|Error {
    return check getVolumnNameLength(path) > 2;
}

function isEmpty(string path) returns boolean {
    return path.length() == 0;
}

function getOffsetIndexes(string path) returns int[]|Error {
    if (isWindows) {
        return check getWindowsOffsetIndex(path);
    } else {
        return check getUnixOffsetIndex(path);
    }
}

function charAt(string input, int index) returns string|Error {
    int length = input.length();
    if (index > length) {
        return GenericError(io:sprintf("Character index %d is greater then path string length %d",
        index, length));
    }
    return input.substring(index, index + 1);
}

function isSamePath(string base, string target) returns boolean {
    if (isWindows) {
        return stringutils:equalsIgnoreCase(base, target);
    } else {
        return base == target;
    }
}
