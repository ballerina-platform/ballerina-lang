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

import ballerina/file;

function testGetTempDir() returns string {
    return file:tempDir();
}

function testRename(string oldpath, string newpath) returns error? {
    return file:rename(oldpath, newpath);
}

function testRemove(string path, boolean recursive) returns error? {
    return file:remove(path, recursive);
}

function testReadDir(string path) returns file:FileInfo[]|error {
    return file:readDir(path);
}

function testReadDirWithMaxDepth(string path, int maxDepth) returns file:FileInfo[]|error {
    return file:readDir(path, maxDepth);
}

function testGetFileInfo(string path) returns file:FileInfo|error {
    return file:getFileInfo(path);
}

function testFileExists(string path) returns boolean {
    return file:exists(path);
}

function testCreateFile(string path) returns string|error {
    return file:createFile(path);
}

function testCreateNonExistingFile(string path) returns string|error {
    return file:createFile(path);
}

function testCreateDir(string dir, boolean parentDirs) returns string|error {
    return file:createDir(dir, parentDirs);
}

function testCopy(string sourcePath, string destinationPath, boolean replaceExisting) returns error? {
    return file:copy(sourcePath, destinationPath, replaceExisting);
}

function testGetCurrentDirectory() returns string {
    return file:getCurrentDirectory();
}
