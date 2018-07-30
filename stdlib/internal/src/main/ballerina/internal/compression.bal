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

documentation {
    Represent all compression related errors.

    F{{message}} The error message
    F{{cause}} The error which caused the compression error
}
public type CompressionError record {
    string message,
    error? cause,
};

documentation {
    Decompresses a byte array into a directory.

    P{{content}} Byte array of the compressed file
    P{{destDir}} Path of the directory to decompress the file
    R{{}} An error if an error occurs during the decompression process
}
public extern function decompressFromByteArray(byte[] content, Path destDir) returns error?;

documentation {
    Decompresses a compressed file.

    P{{dirPath}} Path of the compressed file
    P{{destDir}} Path of the directory to decompress the file
    R{{}} An error if an error occurs during the decompression process
}
public extern function decompress(Path dirPath, Path destDir) returns error?;

documentation {
    Compresses a directory.

    P{{dirPath}} Path of the directory to be compressed
    P{{destDir}} Path of the directory to place the compressed file
    R{{}} An error if an error occurs during the compression process
}
public extern function compress(Path dirPath, Path destDir) returns error?;

documentation {
    Compresses a directory into a byte array.

    P{{dirPath}} Path of the directory to be compressed
    R{{}} Compressed byte array of the file
    R{{}} An error if an error occurs during the compression process
}
public extern function compressToByteArray(Path dirPath) returns byte[]|error;
