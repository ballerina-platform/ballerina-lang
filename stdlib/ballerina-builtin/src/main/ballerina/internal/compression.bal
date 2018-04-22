// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.package internal;


import ballerina/file;

@Description {value:"Represent all compression related errors"}
@Field {value:"message: The error message"}
@Field {value:"cause: The error which caused the compression error"}
public type CompressionError  {
    string message,
    error? cause,
};

@Description {value:"Decompresses a blob into a directory"}
@Param {value:"content: Blob of the compressed file"}
@Param {value:"destDir: Path of the directory to decompress the file"}
@Return {value:"An error if an error occurs during the decompression process"}
public native function decompressFromBlob(blob content, Path destDir) returns error?;

@Description {value:"Decompresses a compressed file"}
@Param {value:"content: Path of the compressed file"}
@Param {value:"destDir: Path of the directory to decompress the file"}
@Return {value:"An error if an error occurs during the decompression process"}
public native function decompress(Path dirPath, Path destDir) returns error?;

@Description {value:"Compresses a directory"}
@Param {value:"dirPath: Path of the directory to be compressed"}
@Param {value:"destDir: Path of the directory to place the compressed file"}
@Return {value:"An error if an error occurs during the compression process"}
public native function compress(Path dirPath, Path destDir) returns error?;

@Description {value:"Compresses a directory into a blob"}
@Param {value:"dirPath: Path of the directory to be compressed"}
@Return {value:"Compressed blob of the file"}
@Return {value:"An error if an error occurs during the compression process"}
public native function compressToBlob(Path dirPath) returns blob|error;