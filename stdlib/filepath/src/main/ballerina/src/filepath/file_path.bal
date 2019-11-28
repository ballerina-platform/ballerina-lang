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
import ballerinax/java;

boolean isWindows = system:getEnv("OS") != "";
string pathSeparator = isWindows ? "\\" : "/";
string pathListSeparator = isWindows ? ";" : ":";

# Retrieves the absolute path from the provided location.
#
# + path - String value of file path.
# + return - The absolute path reference or an error if the path cannot be derived
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

# Returns path separator of underline operating system.
#
# + return - String value of path separator
public function getPathSeparator() returns string {
    return pathSeparator;
}

# Returns path list separator of underline operating system.
#
# + return - String value of path list separator
public function getPathListSeparator() returns string {
    return pathListSeparator;
}

# Reports whether the path is absolute.
# A path is absolute if it is independent of the current directory.
# On Unix, a path is absolute if it starts with the root.
# On Windows, a path is absolute if it has a prefix and starts with the root: c:\windows is absolute
#
# + path - String value of file path.
# + return - True if path is absolute, else false
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

# Retrieves the base name of the file from the provided location.
# The last element of path.
# Trailing path separators are removed before extracting the last element.
#
# + path - String value of file path.
# + return - Returns the name of the file
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
#
# + path - String value of file path.
# + return - Path of parent folder or error occurred while getting parent directory
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

# Returns the shortest path name equivalent to path by purely lexical processing.
# Replace multiple Separator elements with a single one.
# Eliminate each . path name element (the current directory).
# Eliminate each inner .. path name element (the parent directory)
#
# + path - String value of file path.
# + return - Normalized file path
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

# Splits a list of paths joined by the OS-specific Path Separator.
#
# + path - String value of file path.
# + return - String array of part components
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

# Joins any number of path elements into a single path
#
# + parts - String values of file path parts.
# + return - String value of file path.
public function build(string... parts) returns string|Error {
    if (isWindows) {
        return check buildWindowsPath(...parts);
    } else {
        return check buildUnixPath(...parts);
    }
}

# Reports whether the filename is reserved.
# Reserved words only exist in windows.
#
# + name - filename
# + return - true, if path is Windows reserved name.
public function isReservedName(string name) returns boolean {
    if (isWindows) {
        return isWindowsReservedName(name);
    }
    // unix system doesn't have any reserved names.
    return false;
}

# Retrieves the extension of the file path.
# The extension is the suffix beginning at the final dot in the final element of path.
# it is empty if there is no dot.
#
# + path - String value of file path.
# + return - Returns the extension of the file. Empty string if no extension.
public function extension(string path) returns string|Error {
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

# Returns a relative path that is logically equivalent to target path when joined to base path with an intervening
# separator.
# An error is returned if target path can't be made relative to base path.
#
# + base - String value of the base file path.
# + target - String value of the target file path.
# + return - Returns the extension of the file. Empty string if no extension.
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
        return prepareError(RELATIVE_PATH_ERROR, "Can't make: " + target + " relative to " + base);
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
        return prepareError(RELATIVE_PATH_ERROR, "Can't make: " + target + " relative to " + base);
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
# If path is relative, the result will be relative to the current directory,
# unless one of the components is an absolute symbolic link.
# Resolve calls normalize on the result.
#
# + path - String value of file path.
# + return - Resolved file path
public function resolve(@untainted string path) returns string|Error {
    var result = externResolve(java:fromString(path));
    if (result is Error) {
        return result;
    } else {
        var stringResult = java:toString(result);
        if (stringResult is string) {
            return stringResult;
        } else {
            Error e = prepareError(GENERIC_ERROR, "Error occurred while resolving the path.");
            return e;
        }
    }
}

function externResolve(handle path) returns handle|Error =
@java:Method {
    name: "resolve",
    class: "org.ballerinalang.stdlib.filepath.nativeimpl.FilePathUtils"
} external;

# Reports whether all of filename matches the provided pattern, not just a substring.
# An error is returned if the pattern is malformed.
#
# + path - String value of the file path.
# + pattern - String value of the target file path.
# + return - True if filename of the path matches with the pattern, else false
public function matches(string path, string pattern) returns boolean|Error {
    return externMatches(java:fromString(path), java:fromString(pattern));
}

function externMatches(handle path, handle pattern) returns boolean|Error =
@java:Method {
    name: "matches",
    class: "org.ballerinalang.stdlib.filepath.nativeimpl.FilePathUtils"
} external;

# Parses the give path and remove redundent slashes.
#
# + input - string path value
# + return - parsed path,error if given path is invalid.
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
        return prepareError(GENERIC_ERROR, io:sprintf("Character index %d is greater then path string length %d",
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
