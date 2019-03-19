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
        if (word.equalsIgnoreCase(path)) {
            return true;
        }
    }
    return false;
}

function buildWindowsPath(string... parts) returns string|error {
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
    string firstNonEmptyPart = parts[i];
    string root = "";
    int offset;
    (root, offset) = check getWindowsRoot(firstNonEmptyPart);
    string finalPath = "";
    if (root != "" && firstNonEmptyPart.length() <= offset) {
        finalPath = finalPath + root;
    } else {
        finalPath = finalPath + firstNonEmptyPart;
    }
    i = i + 1;
    while (i < count) {
        finalPath = finalPath + "\\." + parts[i];
        i = i + 1;
    }
    return parse(finalPath);
}

function getWindowsRoot(string input) returns (string, int)|error {
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
            if (offset == next) {
                error err = error("{ballerina/path}INVALID_UNC_PATH", { message: "Hostname is missing in UNC path:
                    " + input });
                return err;
            }
            string host = input.substring(offset, next);
            //host
            offset = nextNonSlashIndex(input, next, length);
            next = nextSlashIndex(input, offset, length);
            if (offset == next) {
                error err = error("{ballerina/path}INVALID_UNC_PATH", { message: "Sharename is missing in UNC path:
                    " + input });
                return err;
            }
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
    return (root, offset);
}

function getWindowsOffsetIndex(string path) returns int[] {
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
            if (c == 47 || c == 92) {
                index = index + 1;
            } else {
                offsetIndexes[count] = index;
                count = count + 1;
                index = index + 1;
                while(index < path.length() && (pathValues[index] != 47 || pathValues[index] != 92)) {
                    index = index + 1;
                }
            }
        }
    }
    return offsetIndexes;
}