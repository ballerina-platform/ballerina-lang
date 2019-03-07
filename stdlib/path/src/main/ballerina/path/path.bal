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

public final boolean IS_WINDOWS = system:getEnv("OS") != "" ;
public final string PATH_SEPARATOR = IS_WINDOWS ? "" : "/";
public final byte PATH_SEPARATOR_UTF8 = IS_WINDOWS ? 92 : 47;
public final string PATH_LIST_SEPARATOR = IS_WINDOWS ? ";" : ":";

# Retrieves the absolute path from the provided location.
#
# + path - String value of file path.
# + return - Returns the absolute path reference or an error if the path cannot be derived
public extern function absolute(string path) returns string|error;

# Reports whether the path is absolute.
# A path is absolute if it is independent of the current directory.
# On Unix, a path is absolute if it starts with the root.
# On Windows, a path is absolute if it has a prefix and starts with the root: c:\windows is absolute
# Refer implementation: https://github.com/frohoff/jdk8u-jdk/blob/master/src/windows/classes/sun/nio/fs/WindowsPathParser.java#L94
#
# + path - String value of file path.
# + return - True if path is absolute, else false
public function isAbsolute(string path) returns boolean|error {
    if (path.length() <= 0) {
        return false;
    }
    if (IS_WINDOWS) {
        if (path.length() <= 1) {
            return false;
        }
        string c0 = check charAt(path, 0);
        string c1 = check charAt(path, 1);
        if (isSlash(c0) && isSlash(c1)) {
            return isUNC(path);
        } else {
            if (isLetter(c0) && c1.equalsIgnoreCase(":")) {
                if (path.length() <= 2) {
                    return false;
                }
                string c2 = check charAt(path, 2);
                if (isSlash(c2)) {
                    return true;
                }
                return false;
            }
        }
    } else {
        byte[] bytes = path.toByteArray("UTF-8");
        return bytes[0] == 47;
    }
    return false;
}

# Retrieves the base name of the file from the provided location.
# The last element of path.
# Trailing path separators are removed before extracting the last element.
#
# + path - String value of file path.
# + return - Returns the name of the file
public function filename(string path) returns string|error {
    int[] offsetIndexes = getOffsetIndexes(path);
    int count = offsetIndexes.length();
    if (count == 0) {
        return "";
    }
    if (count == 1 && path.length() > 0 && !(check isAbsolute(path))) {
        return normalizeAndCheck(path);
    }
    int lastOffset = offsetIndexes[count - 1];
    log:printInfo("filename path: " + path);
    return normalizeAndCheck(path.substring(lastOffset, path.length()));
}

function normalizeAndCheck(string input) returns string|error {
    if (input.length() <= 0) {
        return input;
    }
    if (IS_WINDOWS) {
        int length = input.length();
        int offset = 0;
        string root = "";
        if (length > 1) {
            string c0 = check charAt(input, 0);
            string c1 = check charAt(input, 1);
            int next = 2;
            if (isSlash(c0) && isSlash(c1)) {
                boolean unc = check isUNC(input);
                if (!unc) {
                    error err = error("{ballerina/path}INVALID_UNC_PATH", { message: "Invalid UNC path: " + input });
                    return err;
                }
                offset = nextNonSlashIndex(input, next, length);
                next = nextSlashIndex(input, offset, length);
                string host = input.substring(offset, next);  //host
                offset = nextNonSlashIndex(input, next, length);
                next = nextSlashIndex(input, offset, length);
                //TODO remove dot from expression. added because of formatting issue #13872.
                root = "\\\\." + host + "\\." + input.substring(offset, next) + "\\.";
                offset = next;
            } else {
                if (isLetter(c0) && c1.equalsIgnoreCase(":")) {
                    if (input.length() > 2 && isSlash(check charAt(input, 2))) {
                        string c2 = check charAt(input, 2);
                        if (c2 == "\\.") {
                            root = input.substring(0, 3);
                        } else {
                            root = input.substring(0, 2) + "\\.";
                        }
                        offset = 3;
                    } else {
                        root = input.substring(0, 2);
                        offset = 2;
                    }
                }
            }
        }
        return root + check normalizeWindowsPath(input, offset);
    } else {
        int n = input.length();
        string prevC = "";
        int i = 0;
        while (i < n) {
            string c = check charAt(input, i);
            if ((c == "/") && (prevC == "/")) {
                return normalizePosixPath(input, i - 1);
            }
            prevC = c;
            i = i + 1;
        }
        if (prevC == "/") {
            return normalizePosixPath(input, n - 1);
        }
        return input;
    }
}

function normalizeWindowsPath(string path, int off) returns string|error {
    string normalizedPath = "";
    int length = path.length();
    int offset = nextNonSlashIndex(path, off, length);
    int startIndex = offset;
    string lastC = "";
    while (offset < length) {
        string c = check charAt(path, offset);
        if (isSlash(c)) {
            normalizedPath = normalizedPath + path.substring(startIndex, offset);
            offset = nextNonSlashIndex(path, offset, length);
            if (offset != length) {
                normalizedPath = normalizedPath + "\\.";
            }
            startIndex = offset;
        } else {
            lastC = c;
            offset = offset + 1;
        }
    }
    if (startIndex != offset) {
        normalizedPath = normalizedPath + path.substring(startIndex, offset);
    }
    return normalizedPath;
}

function normalizePosixPath(string input, int off) returns string|error {
    int n = input.length();
    byte[] bytes = input.toByteArray("UTF-8");
    while((n > 0) && (bytes[n-1] == 47)) {
        n = n-1;
    }
    if (n == 0) {
        return "/";
    }
    string normalizedPath = "";
    if (off > 0) {
        normalizedPath = normalizedPath + input.substring(0, off);
    }
    string prevC = "";
    int i = off;
    while(i < n) {
        string c = check charAt(input, i);
        if (c == "/" && prevC == "/") {
            continue;
        }
        normalizedPath = normalizedPath + c;
        prevC = c;
        i = i + 1;
    }
    return normalizedPath;
}

function isSlash(string|byte c) returns boolean {
    if (c is string) {
        return (c == "") || (c == "/");
    } else {
        return (c == 92 || c == 47);
    }
}

function nextNonSlashIndex(string path, int offset, int end) returns int {
    byte[] pathValues = path.toByteArray("UTF-8");
    int off = offset;
    while(off < end && isSlash(pathValues[off])) {
        off = off + 1;
    }
    return off;
}

function nextSlashIndex(string path, int offset, int end) returns int {
    byte[] pathValues = path.toByteArray("UTF-8");
    int off = offset;
    while(off < end && !isSlash(pathValues[off])) {
        off = off + 1;
    }
    return off;
}

function isLetter(string c) returns boolean {
    string regEx = "^[a-zA-Z]{1}$";
    boolean|error letter = c.matches(regEx);
    if (letter is error) {
        log:printError("Error while checking input character is string", err = letter);
        return false;
    } else {
        return letter;
    }
}

function isUNC(string path) returns boolean|error {
    string regEx = "\\\\[a-zA-Z0-9.-_]{1,}(\\[a-zA-Z0-9-_]{1,}){1,}[$]{0,1}";
    boolean|error output = path.matches(regEx);
    if (output is error) {
        error err = error("{ballerina/path}INVALID_UNC_PATH", output.detail());
        return err;
    } else {
        return output;
    }
}

function isEmpty(string path) returns boolean {
    return path.length() == 0;
}

function getOffsetIndexes(string path) returns int[] {
    int[] offsetIndexes = [];
    int index = 0;
    int count = 0;
    if (isEmpty(path)) {
        offsetIndexes[count] = 0;
        count = count + 1;
    } else {
        byte[] pathValues = path.toByteArray("UTF-8");
        while(index < path.length()) {
            byte c = pathValues[index];
            if (c == PATH_SEPARATOR_UTF8) {
                index = index + 1;
            } else {
                offsetIndexes[count] = index;
                count = count + 1;
                index = index + 1;
                while(index < path.length() && pathValues[index] != PATH_SEPARATOR_UTF8) {
                    index = index + 1;
                }
            }
        }
    }
    return offsetIndexes;
}

function charAt(string input, int index) returns string|error {
    int length = input.length();
    if (index > length) {
        error err = error("{ballerina/path}INVALID_OPERATION",
        { message: io:sprintf("Character index %d is greater then path string length %d", index, length) });
        return err;
    }
    return input.substring(index, index + 1);
}
