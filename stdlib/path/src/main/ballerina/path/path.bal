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

import ballerina/log;
import ballerina/system;

public final boolean IS_WINDOWS = system:getEnv("OS") != "" ;
public final string PATH_SEPARATOR = IS_WINDOWS ? "" : "/";
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
#
# + path - String value of file path.
# + return - True if path is absolute, else false
public function isAbsolute(string path) returns boolean {
    if (path.length() <= 0) {
        return false;
    }
    if (IS_WINDOWS) {
        if (path.length() <= 1) {
            return false;
        }
        string c0 = path.substring(0,1);
        string c1 = path.substring(1,2);
        if (isSlash(c0) && isSlash(c1)) {
            var unc = isUNC(path);
            if (unc is error) {
                log:printError("Error while checking whether path has valid UNC format", err = unc);
                return false;
            } else {
                if (!unc) {
                    log:printError("Invaild UNC path");
                }
                return unc;
            }
        } else {
            if (isLetter(c0) && c1.equalsIgnoreCase(":")) {
                if (path.length() <= 2) {
                    return false;
                }
                string c2 = path.substring(2,3);
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

function isSlash(string c) returns boolean {
    return (c == "") || (c == "/");
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
    return path.matches(regEx);
}
