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

import ballerina/path;

function testGetAbsolutePath(string path) returns string|error {
    return path:absolute(path);
}

function testGetPathSeparator() returns string {
    return path:getPathSeparator();
}

function testGetPathListSeparator() returns string {
    return path:getPathListSeparator();
}

function testIsAbsolutePath(string path) returns boolean|error {
    return path:isAbsolute(path);
}

function testGetFilename(string path) returns string|error {
    return path:filename(path);
}

function testGetParent(string path) returns string|error {
    return path:parent(path);
}

function testNormalizePath(string path) returns string|error {
    return path:normalize(path);
}

function testSplitPath(string path) returns string[]|error {
    return path:split(path);
}

function testBuildPath(string[] paths) returns string|error {
    return path:build(...paths);
}

function testPathExtension(string path) returns string|error {
    return path:extension(path);
}

function testRelativePath(string base, string target) returns string|error {
    return path:relative(base, target);
}