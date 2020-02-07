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

import ballerina/filepath;

function testGetAbsolutePath(string path) returns string|error {
    return filepath:absolute(path);
}

function testGetPathSeparator() returns string {
    return filepath:getPathSeparator();
}

function testGetPathListSeparator() returns string {
    return filepath:getPathListSeparator();
}

function testIsAbsolutePath(string path) returns boolean|error {
    return filepath:isAbsolute(path);
}

function testGetFilename(string path) returns string|error {
    return filepath:filename(path);
}

function testGetParent(string path) returns string|error {
    return filepath:parent(path);
}

function testNormalizePath(string path) returns string|error {
    return filepath:normalize(path);
}

function testSplitPath(string path) returns string[]|error {
    return filepath:split(path);
}

function testBuildPath(string[] paths) returns string|error {
    return filepath:build(...paths);
}

function testPathExtension(string path) returns string|error {
    return filepath:extension(path);
}

function testRelativePath(string base, string target) returns string|error {
    return filepath:relative(base, target);
}

function testResolvePath(string path) returns string|error {
    return filepath:resolve(path);
}

function testPathMatches(string path, string pattern) returns boolean|error {
    return filepath:matches(path, pattern);
}
