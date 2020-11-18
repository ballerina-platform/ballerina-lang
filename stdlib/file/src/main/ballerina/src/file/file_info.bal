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

import ballerina/time;

# FileInfo record contains metadata information of a file.
# This record is returned by getFileInfo function is os module.
public class FileInfo {

    string name;
    int size;
    time:Time modifiedTime;
    boolean dir;
    string path;

    # Creates a FileInfo object.
    #
    # + name - Name of the file
    # + size - Size of the file (in bytes)
    # + modifiedTime - The last modified time of the file
    # + dir - Whether the file is a directory or not
    # + path - Absolute path of the file
    public function init(string name, int size, time:Time modifiedTime, boolean dir, string path) {
        self.name = name;
        self.size = size;
        self.modifiedTime = modifiedTime;
        self.dir = dir;
        self.path = path;
    }

    # Returns the file name.
    #
    # + return - the file name
    public function getName() returns string {
        return self.name;
    }

    # Returns the file size.
    #
    # + return - the file size
    public function getSize() returns int {
        return self.size;
    }

    # Returns the last-modified time of the file.
    #
    # + return - Last-modified time of the file
    public function getLastModifiedTime() returns time:Time {
        return self.modifiedTime;
    }

    # Returns whether the file is a directory or not.
    #
    # + return - File is a directory or not
    public function isDir() returns boolean {
        return self.dir;
    }

    # Returns the absolute file path.
    #
    # + return - The file path
    public function getPath() returns string {
        return self.path;
    }
}
