// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/internal;
import ballerina/log;
import ballerina/time;

string TEST_CONTENT = "Hello";

function createSourceAndTargetDirs(string pathValue) returns boolean {
    internal:Path parentPath = new(pathValue);
    internal:Path sourcePath = parentPath.resolve("source");
    match sourcePath.createDirectory() {
        () => {
            internal:Path targetPath = parentPath.resolve("target");
            match targetPath.createDirectory() {
                () => {
                    return sourcePath.exists() && targetPath.exists();
                }
                error err => {
                    log:printError("Error occurred while creating target directory.", err = err);
                    return false;
                }
            }
        }
        error err => {
            log:printError("Error occurred while creating source directory.", err = err);
            return false;
        }
    }
}

function createFolderStructure(string sourcePathValue) returns boolean {
    internal:Path sourcePath = new(sourcePathValue);
    internal:Path parentPath = sourcePath.resolve("parent");
    match parentPath.createDirectory() {
        () => {
            internal:Path childPath = parentPath.resolve("child");
            match childPath.createDirectory() {
                () => {
                    internal:Path xmlFilePath = childPath.resolve("sample.xml");
                    match xmlFilePath.createFile() {
                        () => {
                            return parentPath.exists() && childPath.exists() && xmlFilePath.exists();
                        }
                        error err => {
                            log:printError("Error occurred while creating sample.xml file.", err = err);
                            return false;
                        }
                    }
                }
                error err => {
                    log:printError("Error occurred while creating child directory.", err = err);
                    return false;
                }
            }
        }
        error err => {
            log:printError("Error occurred while creating parent directory.", err = err);
            return false;
        }
    }
}

function testFolderContent(string rootPathValue) returns boolean {
    internal:Path rootPath = new(rootPathValue);
    internal:Path parentPath = rootPath.resolve("parent");
    internal:Path childPath = parentPath.resolve("child");
    internal:Path xmlFilePath = rootPath.resolve("parent", "child", "sample.xml");

    _ = testWriteFile(xmlFilePath.getPathValue());

    internal:Path parentOfXmlFile = check xmlFilePath.getParentDirectory();
    internal:Path[] paths = check parentOfXmlFile.list();

    return parentPath.getExtension() == "" &&
            parentPath.isDirectory() &&
            parentPath.exists() &&
            childPath.exists() &&
            childPath.isDirectory() &&
            !childPath.resolve("abc").exists() &&
            xmlFilePath.exists() &&
            xmlFilePath.getExtension() == "xml" &&
            parentOfXmlFile.getPathValue() == childPath.getPathValue() &&
            xmlFilePath.getName() == "sample.xml" &&
            testReadFile(xmlFilePath.getPathValue()) &&
            lengthof paths == 1;
}

function testGetModifiedTime(string pathValue) returns (string) {
    internal:Path path = new(pathValue);
    time:Time modifiedTime = check path.getModifiedTime();
    return modifiedTime.toString();
}

function testCopyToFunction(string source, string target) returns boolean {
    internal:Path sourcePath = new(source);
    internal:Path targetPath = new(target);
    match sourcePath.copyTo(targetPath) {
        () => {
            return testFolderContent(targetPath.getPathValue());
        }
        error err => {
            log:printError("Error occurred while creating target directory.", err = err);
            return false;
        }
    }
}

function testFolderDelete(string path) returns boolean {
    internal:Path pathToDelete = new(path);
    match pathToDelete.delete() {
        () => {
            return !pathToDelete.exists();
        }
        error err => {
            log:printError("Error occurred while deleting directory: " + path, err = err);
            return false;
        }
    }
}

function testMoveToFunction(string source, string target) returns boolean {
    internal:Path sourcePath = new(source);
    internal:Path targetPath = new(target);
    match sourcePath.moveTo(targetPath) {
        () => {
            return testFolderContent(targetPath.getPathValue());
        }
        error err => {
            log:printError("Error occurred while creating target directory.", err = err);
            return false;
        }
    }
}

function testWriteFile(string pathValue) returns error? {
    internal:Path filePath = new(pathValue);
    string absolutePath = filePath.getPathValue();
    io:WritableByteChannel byteChannel = io:openWritableFile(absolutePath);
    var result = byteChannel.write(TEST_CONTENT.toByteArray("UTF-8"), 0);
    return byteChannel.close();
}

function testReadFile(string pathValue) returns boolean {
    io:ReadableByteChannel byteChannel = io:openReadableFile(pathValue);
    var readResult = byteChannel.read(100);
    _ = byteChannel.close();
    match readResult {
        (byte[], int) byteContent => {
            var (bytes, numberOfBytes) = byteContent;
            return lengthof bytes == lengthof TEST_CONTENT.toByteArray("UTF-8");
        }
        error err => {
            log:printError("Error occurred while reading content: " + pathValue, err = err);
            return false;
        }
    }
}