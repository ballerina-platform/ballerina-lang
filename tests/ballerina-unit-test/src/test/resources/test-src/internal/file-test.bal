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
const string INTERNAL_ERROR_CODE = "{ballerina/internal}InternalError";

function createSourceAndTargetDirs(string pathValue) returns boolean {
    internal:Path parentPath = new(pathValue);
    internal:Path sourcePath = parentPath.resolve("source");
    var createDirResult = sourcePath.createDirectory();
    if (createDirResult is error) {
        log:printError("Error occurred while creating source directory.", err = createDirResult);
        return false;
    } else {
        internal:Path targetPath = parentPath.resolve("target");
        var targetCreDirResult = targetPath.createDirectory();
        if (targetCreDirResult is error) {
            log:printError("Error occurred while creating target directory.", err = targetCreDirResult);
            return false;
        } else {
            return sourcePath.exists() && targetPath.exists();
        }
    }
}

function createFolderStructure(string sourcePathValue) returns boolean {
    internal:Path sourcePath = new(sourcePathValue);
    internal:Path parentPath = sourcePath.resolve("parent");
    var parentCreDirResult = parentPath.createDirectory();
    if (parentCreDirResult is error) {
        log:printError("Error occurred while creating parent directory.", err = parentCreDirResult);
        return false;
    } else {
        internal:Path childPath = parentPath.resolve("child");
        var childCreDirResult = childPath.createDirectory();
        if (childCreDirResult is error) {
            log:printError("Error occurred while creating child directory.", err = childCreDirResult);
            return false;
        } else {
            internal:Path xmlFilePath = childPath.resolve("sample.xml");
            var createFileResult = xmlFilePath.createFile();
            if (createFileResult is error) {
                log:printError("Error occurred while creating sample.xml file.", err = createFileResult);
                return false;
            } else {
                return parentPath.exists() && childPath.exists() && xmlFilePath.exists();
            }
        }
    }
}

function testFolderContent(string rootPathValue) returns boolean|error {
    internal:Path rootPath = new(rootPathValue);
    internal:Path parentPath = rootPath.resolve("parent");
    internal:Path childPath = parentPath.resolve("child");
    internal:Path xmlFilePath = rootPath.resolve("parent", "child", "sample.xml");

    _ = testWriteFile(xmlFilePath.getPathValue());

    var parentDirectory = xmlFilePath.getParentDirectory();
    if (parentDirectory is internal:Path) {
        var paths = parentDirectory.list();
        if (paths is internal:Path[]) {
            return parentPath.getExtension() == "" &&
                parentPath.isDirectory() &&
                parentPath.exists() &&
                childPath.exists() &&
                childPath.isDirectory() &&
                !childPath.resolve("abc").exists() &&
                xmlFilePath.exists() &&
                xmlFilePath.getExtension() == "xml" &&
                parentDirectory.getPathValue() == childPath.getPathValue() &&
                xmlFilePath.getName() == "sample.xml" &&
                testReadFile(xmlFilePath.getPathValue()) &&
                paths.length() == 1;
        } else {
            error listNotFound = error(INTERNAL_ERROR_CODE, { message : "File list fetching error" });
            return listNotFound;
        }
    } else {
        error directoryNotFound = error(INTERNAL_ERROR_CODE, { message : "Directory not found" });
        return directoryNotFound;
    }
}

function testGetModifiedTime(string pathValue) returns string|error {
    internal:Path path = new(pathValue);
    time:Time modifiedTime = check path.getModifiedTime();
    return modifiedTime.toString();
}

function testCopyToFunction(string source, string target) returns boolean {
    internal:Path sourcePath = new(source);
    internal:Path targetPath = new(target);
    var result = sourcePath.copyTo(targetPath);
    if (result is error) {
        log:printError("Error occurred while creating target directory.", err = result);
        return false;
    } else {
        var contentResult = testFolderContent(targetPath.getPathValue());
        if (contentResult is boolean) {
            return contentResult;
        } else {
            log:printError("Error occurred getting folder content.", err = contentResult);
            return false;
        }
    }
}

function testFolderDelete(string path) returns boolean {
    internal:Path pathToDelete = new(path);
    var deleteResult = pathToDelete.delete();
    if (deleteResult is error) {
        log:printError("Error occurred while deleting directory: " + path, err = deleteResult);
        return false;
    } else {
        return !pathToDelete.exists();
    }
}

function testMoveToFunction(string source, string target) returns boolean {
    internal:Path sourcePath = new(source);
    internal:Path targetPath = new(target);
    var moveResult = sourcePath.moveTo(targetPath);
    if (moveResult is error) {
        log:printError("Error occurred while creating target directory.", err = moveResult);
        return false;
    } else {
        var contentResult = testFolderContent(targetPath.getPathValue());
        if (contentResult is boolean) {
            return contentResult;
        } else {
            log:printError("Error occurred getting folder content.", err = contentResult);
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
    if (readResult is error) {
        log:printError("Error occurred while reading content: " + pathValue, err = readResult);
        return false;
    } else {
        var (bytes, numberOfBytes) = readResult;
        return bytes.length() == TEST_CONTENT.toByteArray("UTF-8").length();
    }
}
