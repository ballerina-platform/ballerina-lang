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

boolean IS_WINDOWS = system:getEnv("OS") != "";
string PATH_SEPARATOR = IS_WINDOWS ? "\\" : "/";
byte PATH_SEPARATOR_UTF8 = IS_WINDOWS ? 92 : 47;
string PATH_LIST_SEPARATOR = IS_WINDOWS ? ";" : ":";

# Retrieves the absolute path from the provided location.
#
# + path - String value of file path.
# + return - The absolute path reference or an error if the path cannot be derived
public extern function absolute(string path) returns string|error;

# Returns path separator of underline operating system.
#
# + return - String value of path separator
public function getPathSeparator() returns string {
    return PATH_SEPARATOR;
}

# Returns path list separator of underline operating system.
#
# + return - String value of path list separator
public function getPathListSeparator() returns string {
    return PATH_LIST_SEPARATOR;
}

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
    string validatedPath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(validatedPath);
    log:printInfo("Validated file path: " + validatedPath);
    int count = offsetIndexes.length();
    if (count == 0) {
        return "";
    }
    if (count == 1 && validatedPath.length() > 0) {
        if !(check isAbsolute(validatedPath)) {
            return validatedPath;
        } else if (IS_WINDOWS) {
            return "";
        }
    }
    int lastOffset = offsetIndexes[count - 1];
    return validatedPath.substring(lastOffset, validatedPath.length());
}

# Get the enclosing parent directory.
# If the path is empty, Dir returns ".".
# The returned path does not end in a separator unless it is the root directory.
#
# + path - String value of file path.
# + return - Path of parent folder or error occurred while getting parent directory
public function parent(string path) returns string|error {
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
    (root, offset) = check getRootComponent(validatedPath);
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
public function normalize(string path) returns string|error {
    string filepath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(filepath);
    int count = offsetIndexes.length();
        io:println("filepath (normalize) : " + filepath);
        io:println(offsetIndexes);
    if (count == 0 || isEmpty(filepath)) {
        return filepath;
    }

    boolean abs = check isAbsolute(filepath);
    string root;
    int offset;
    (root, offset) = check getRootComponent(filepath);
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
            length = filepath.length() - begin;
            parts[i] = filepath.substring(begin, filepath.length());
        } else {
            length = offsetIndexes[i + 1] - begin - 1;
            parts[i] = filepath.substring(begin, offsetIndexes[i + 1] - 1);
        }
        if (check charAt(filepath, begin) == ".") {
            if (length == 1) {
                ignore[i] = true;
                remaining = remaining - 1;
            } else if (length == 2 && check charAt(filepath, begin + 1) == ".") {
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
                if (hasPrevious || abs || isSlash(c0)) {
                    ignore[i] = true;
                    remaining = remaining - 1;
                }  
            }
        }
        i = i + 1;
    }

    if (remaining == count) {
        return filepath;
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
            normalizedPath = normalizedPath + parts[i] + PATH_SEPARATOR;
        }
        i = i + 1;
    }
    return parse(normalizedPath);
}

# Splits a list of paths joined by the OS-specific Path Separator.
#
# + path - String value of file path.
# + return - String array of part components
public function split(string path) returns string[]|error {
    string filepath = check parse(path);
    int[] offsetIndexes = check getOffsetIndexes(filepath);
    int count = offsetIndexes.length();

    string[] parts = [];
    int i = 0;
    while (i < count) {
        int begin = offsetIndexes[i];
        int length;
        if (i == (count - 1)) {
            length = filepath.length() - begin;
            parts[i] = check parse(filepath.substring(begin, filepath.length()));
        } else {
            length = offsetIndexes[i + 1] - begin - 1;
            parts[i] = check parse(filepath.substring(begin, offsetIndexes[i + 1] - 1));
        }
        i = i + 1;
    }
    return parts;
}

# Joins any number of path elements into a single path
#
# + parts - String values of file path parts.
# + return - String value of file path.
public function build(string... parts) returns string|error {
    if (IS_WINDOWS) {
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
    if (IS_WINDOWS) {
        return isWindowsReservedName(name);
    }
    // unix system doesn't have any reserved names.
    return false;
}

# Retrieves the extension of the file from the provided location.
# The extension is the suffix beginning at the final dot in the final element of path.
# it is empty if there is no dot.
#
# + path - String value of file path.
# + return - Returns the extension of the file. Empty string if no extension.
public function extension(string path) returns string|error {
    string filepath = check parse(path);
    int count = filepath.length();
    if (count == 0) {
        return filepath;
    }
    int i = count - 1;
    while (i >= 0) {
        string char = check charAt(filepath, i);
        if (char == PATH_SEPARATOR) {
            break;
        }
        if (char == ".") {
            return filepath.substring(i + 1, count);
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
public function relative(string base, string target) returns string|error {
    string cleanBase = check normalize(base);
    string cleanTarget = check normalize(target);
    if (isSamePath(cleanBase, cleanTarget)) {
        return ".";
    }
    string baseRoot;
    int baseOffset;
    (baseRoot, baseOffset) = check getRootComponent(cleanBase);
    string targetRoot;
    int targetOffset;
    (targetRoot, targetOffset) = check getRootComponent(cleanTarget);
    if (!isSamePath(baseRoot, targetRoot)) {
        error err = error("{ballerina/path}RELATIVE_PATH_ERROR", { message: "Can't make: " + target + " relative to " +
            base});
        return err;
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
        error err = error("{ballerina/path}RELATIVE_PATH_ERROR", { message: "Can't make: " + target + " relative to " +
            base});
        return err;
    }
    if (b0 != bl) {
        string remainder = cleanBase.substring(b0, bl);
        int[] offsets = check getOffsetIndexes(remainder);
        int noSeparators = offsets.length() - 1;
        string relativePath = "..";
        int i = 0;
        while (i < noSeparators) {
            relativePath = relativePath + PATH_SEPARATOR + "..";
            i = i + 1;
        }
        if (t0 != tl) {
            relativePath = relativePath + PATH_SEPARATOR + cleanTarget.substring(t0, tl);
        }
        return relativePath;
    }
    return cleanTarget.substring(t0, tl);
}

# Reports whether all of filename matches the provided pattern, not just a substring.
# An error is returned if the pattern is malformed.
#
# + path - String value of the file path.
# + pattern - String value of the target file path.
# + return - True if filename of the path matches with the pattern, else false
public function matches(string path, string pattern) returns boolean|error {
    
    return false;
}

# Parses the give path and remove redundent slashes.
#
# + input - string path value
# + return - parsed path,error if given path is invalid.
function parse(string input) returns string|error {
    if (input.length() <= 0) {
        return input;
    }
    if (IS_WINDOWS) {
        int offset = 0;
        string root = "";
        (root, offset) = check getRootComponent(input);
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

function getRootComponent(string input) returns (string,int)|error {
    if (IS_WINDOWS) {
        return getWindowsRoot(input);
    } else {
        return getUnixRoot(input);
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
                normalizedPath = normalizedPath + "\\";
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
            i = i + 1;
            continue;
        }
        normalizedPath = normalizedPath + c;
        prevC = c;
        i = i + 1;
    }
    return normalizedPath;
}

function isSlash(string|byte c) returns boolean {
    if (IS_WINDOWS) {
        return isWindowsSlash(c);
    } else {
        return isPosixSlash(c);
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

function getRootOffset(string path) returns int|error {
	if path.length() < 2 {
		return 0;
	}
	// check driver
	string c0 = check charAt(path, 0);
	string c1 = check charAt(path, 1);
	if (c1 == ":" && isLetter(c0)) {
		return 2;
	}
	int size = path.length();
	string c2 = check charAt(path, 2);
	if (size >= 5 && isSlash(c0) && isSlash(c1) && !isSlash(c2) && c2 != ".") {
		// first, leading `\\` and next shouldn't be `\`. its server name.
		int n = 3;
		while (n < size-1) {
			// second, next '\' shouldn't be repeated.
			string cn = check charAt(path, n);
			if isSlash(cn) {
				n = n + 1;
				cn = check charAt(path, n);
				// third, share name.
				if !isSlash(cn) {
					if cn == "." {
						break;
					}

					while(n < size) {
						if isSlash(cn) {
							break;
						}
						n = n + 1;
					}
					return n;
				}
				break;
			}
			n = n + 1;
		}
	}
	return 0;
}

function isUNC(string path) returns boolean|error {
    return check getRootOffset(path) > 2;
}

function isEmpty(string path) returns boolean {
    return path.length() == 0;
}

function getOffsetIndexes(string path) returns int[]|error {
    if (IS_WINDOWS) {
        return check getWindowsOffsetIndex(path);
    } else {
        return getUnixOffsetIndex(path);
    }
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

function isSamePath(string base, string target) returns boolean {
    if (IS_WINDOWS) {
        return base.equalsIgnoreCase(target);
    } else {
        return base == target;
    }
}
