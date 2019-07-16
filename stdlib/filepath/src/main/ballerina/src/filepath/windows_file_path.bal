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

import ballerina/internal;

// ReservedNames lists reserved Windows names.
// https://docs.microsoft.com/en-us/windows/desktop/fileio/naming-a-file for details.
string[] WINDOWS_RESERVED_WORDS = ["CON", "PRN", "AUX", "NUL",
	"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
	"LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"];

function isWindowsReservedName(string path) returns boolean {
    if (path.length() == 0) {
        return false;
    }
    foreach string word in WINDOWS_RESERVED_WORDS {
        if (internal:equalsIgnoreCase(word, path)) {
            return true;
        }
    }
    return false;
}

function buildWindowsPath(string... parts) returns string|Error {
    int count = parts.length();
    if (count <= 0) {
        return "";
    }
    int i = 0;
    while (i < count) {
        if (parts[i] != "") {
            break;
        }
        i = i + 1;
    }
    if (i == count) {
        return "";
    }
    string firstNonEmptyPart = parts[i];

    if (firstNonEmptyPart.length() == 2) {
        string c0 = check charAt(firstNonEmptyPart, 0);
        string c1 = check charAt(firstNonEmptyPart, 1);
        if (isLetter(c0) && internal:equalsIgnoreCase(c1, ":")) {
            // First element is driver letter without terminating slash.
            i = i + 1;
            while (i < count) {
                if (parts[i] != "") {
                    break;
                }
                i = i + 1;
            }
            string tail;
            if (i < count) {
                tail = parts[i];
                i = i + 1;
            } else {
                return normalize(firstNonEmptyPart);
            }

            while(i < count) {
                if (parts[i] != "") {
                    tail = tail + "\\" + parts[i];
                }
                i = i + 1;
            }
            return firstNonEmptyPart + check normalize(tail);
        }
    }

    // UNC only allowed when the first element is a UNC path.
    string head = firstNonEmptyPart;
    if (check isUNC(head)) {
        string finalPath = firstNonEmptyPart;
        i = i + 1;
        while(i < count) {
            finalPath = finalPath + "\\" + parts[i];
            i = i + 1;
        }
        return normalize(finalPath);
    }

    i = i + 1;
    string tail;
    if (i < count) {
        tail = parts[i];
        i = i + 1;
    } else {
        return normalize(firstNonEmptyPart);
    }

    while(i < count) {
        if (parts[i] != "") {
            tail = tail + "\\" + parts[i];
        }
        i = i + 1;
    }
    string normalizedHead = check normalize(head);
    string normalizedTail = check normalize(tail);

    if (tail == "") {
        return normalizedHead;
    }
    int index = check nextNonSlashIndex(normalizedTail, 0, normalizedTail.length());
    if (index > 0) {
        normalizedTail = normalizedTail.substring(index, normalizedTail.length());
    }

    if check charAt(normalizedHead, normalizedHead.length() - 1) == pathSeparator {
		return normalizedHead + normalizedTail;
	}
	return normalizedHead + pathSeparator + normalizedTail;
}

function getWindowsRoot(string input) returns [string, int]|Error {
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
                return prepareError(message = "Invalid UNC path: " + input);
            }
            offset = check nextNonSlashIndex(input, next, length);
            next = check nextSlashIndex(input, offset, length);
            if (offset == next) {
                return prepareError(message = "Hostname is missing in UNC path: " + input);
            }
            string host = input.substring(offset, next);  //host
            offset = check nextNonSlashIndex(input, next, length);
            next = check nextSlashIndex(input, offset, length);
            if (offset == next) {
                return prepareError(message = "Sharename is missing in UNC path: " + input);
            }
            //TODO remove dot from expression. added because of formatting issue #13872.
            root = "\\\\" + host + "\\" + input.substring(offset, next) + "\\";
            offset = next;
        } else if (isSlash(c0)) {
            root = "\\";
            offset = 1;
        } else {
            if (isLetter(c0) && internal:equalsIgnoreCase(c1, ":")) {
                if (input.length() > 2 && isSlash(check charAt(input, 2))) {
                    string c2 = check charAt(input, 2);
                    if (c2 == "\\") {
                        root = input.substring(0, 3);
                    } else {
                        root = input.substring(0, 2) + "\\";
                    }
                    offset = 3;
                } else {
                    root = input.substring(0, 2);
                    offset = 2;
                }
            }
        }
    } else if (length > 0 && isSlash(check charAt(input, 0))) {
            root = "\\";
            offset = 1;
    }
    return [root, offset];
}

function getWindowsOffsetIndex(string path) returns int[]|Error {
    int[] offsetIndexes = [];
    int index = 0;
    int count = 0;
    if (isEmpty(path)) {
        offsetIndexes[count] = 0;
        count = count + 1;
    } else {
        [_, index] = check getWindowsRoot(path);
        while(index < path.length()) {
            string cn = check charAt(path, index);
            if (cn == "/" || cn == "\\") {
                index = index + 1;
            } else {
                offsetIndexes[count] = index;
                count = count + 1;
                index = index + 1;
                while(index < path.length()) {
                    cn = check charAt(path, index);
                    if (cn == "/" || cn == "\\") {
                        break;
                    }
                    index = index + 1;
                }
            }
        }
    }
    return offsetIndexes;
}

function isWindowsSlash(string c) returns boolean {
    return (c == "\\") || (c == "/");
}

# Returns length of windows volumn length.
# If volumn doesn't exist, return 0
#
# + path - string path value
# + return - windows volumn length
function getVolumnNameLength(string path) returns int|Error {
    if path.length() < 2 {
        return 0;
    }
    // check driver
    string c0 = check charAt(path, 0);
    string c1 = check charAt(path, 1);
    if (isLetter(c0) && c1 == ":") {
        return 2;
    }
    int size = path.length();
    if (size < 5) {
        return 0;
    }
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

function parseWindowsPath(string path, int off) returns string|Error {
    string normalizedPath = "";
    int length = path.length();
    int offset = check nextNonSlashIndex(path, off, length);
    int startIndex = offset;
    string lastC = "";
    while (offset < length) {
        string c = check charAt(path, offset);
        if (isSlash(c)) {
            normalizedPath = normalizedPath + path.substring(startIndex, offset);
            offset = check nextNonSlashIndex(path, offset, length);
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
