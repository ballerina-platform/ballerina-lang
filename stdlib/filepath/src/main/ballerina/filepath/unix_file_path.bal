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

function buildUnixPath(string... parts) returns string|error {
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
    string finalPath = parts[i];
    i = i + 1;
    while (i < count) {
        finalPath = finalPath + "/" + parts[i];
        i = i + 1;
    }
    return parse(finalPath);
}

function getUnixRoot(string input) returns [string, int]|error {
    int length = input.length();
    int offset = 0;
    string root = "";
    if (length > 0 && isSlash(check charAt(input, 0))) {
        root = pathSeparator;
        offset = 1;
    }
    return [root, offset];
}

function getUnixOffsetIndex(string path) returns int[]|error {
    int[] offsetIndexes = [];
    int index = 0;
    int count = 0;
    if (isEmpty(path)) {
        offsetIndexes[count] = 0;
        count = count + 1;
    } else {
        while(index < path.length()) {
            string cn = check charAt(path, index);
            if (cn == "/") {
                index = index + 1;
            } else {
                offsetIndexes[count] = index;
                count = count + 1;
                index = index + 1;
                while(index < path.length()) {
                    cn = check charAt(path, index);
                    if (cn == "/") {
                        break;
                    }
                    index = index + 1;
                }
            }
        }
    }
    return offsetIndexes;
}

function isPosixSlash(string|byte c) returns boolean {
    return c == "/";
}

function parsePosixPath(string input, int off) returns string|error {
    int n = input.length();
    while((n > 0)) {
        string cn = check charAt(input, n-1);
        if(cn != "/") {
            break;
        }
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
