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

# Represent all compression related errors.
#
# + message - The error message
# + cause - The error which caused the compression error
public type CompressionError record {
    string message;
    error? cause;
    !...
};

# Decompresses a byte array into a directory.
#
# + content - Byte array of the compressed file
# + destDir - Path of the directory to decompress the file
# + return - An error if an error occurs during the decompression process
public extern function decompressFromByteArray(byte[] content, Path destDir) returns CompressionError?;

# Decompresses a compressed file.
#
# + dirPath - Path of the compressed file
# + destDir - Path of the directory to decompress the file
# + return - An error if an error occurs during the decompression process
public extern function decompress(Path dirPath, Path destDir) returns CompressionError?;

# Compresses a directory.
#
# + dirPath - Path of the directory to be compressed
# + destDir - Path of the directory to place the compressed file
# + return - An error if an error occurs during the compression process
public extern function compress(Path dirPath, Path destDir) returns CompressionError?;

# Compresses a directory into a byte array.
#
# + dirPath - Path of the directory to be compressed
# + return - Compressed byte array of the file.
#            An error if an error occurs during the compression process.
public extern function compressToByteArray(Path dirPath) returns byte[]|CompressionError;
