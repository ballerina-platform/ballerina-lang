// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Retrieves a `ReadableByteChannel` from a given file path.
#```ballerina
# io:ReadableByteChannel readableFieldResult = check io:openReadableFile("./files/sample.txt");
#```
#
# + path - Relative/absolute path string to locate the file
# + return - The `ByteChannel` representation of the file resource or else an `io:Error` if any error occurred
public function openReadableFile(@untainted string path) returns ReadableByteChannel|Error = @java:Method {
    name: "openReadableFile",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

# Retrieves a `WritableByteChannel` from a given file path.
#```ballerina
# io:WritableByteChannel writableFileResult = check io:openWritableFile("./files/sampleResponse.txt");
# ```
#
# + path - Relative/absolute path string to locate the file
# + append - Whether to append to the end of file
# + return - The `ByteChannel` representation of the file resource or else an `io:Error` if any error occurred
public function openWritableFile(@untainted string path, boolean append = false)
    returns WritableByteChannel|Error = @java:Method {
    name: "openWritableFile",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

# Creates an in-memory channel, which will be a reference stream of bytes.
# ```ballerina
# var byteChannel = io:createReadableChannel(content);
# ```
#
# + content - Content, which should be exposed as a channel
# + return - The `ByteChannel` representation to read the memory content or else an `io:Error` if any error occurred
public function createReadableChannel(byte[] content) returns ReadableByteChannel|Error = @java:Method {
    name: "createReadableChannel",
    'class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

# Retrieves a readable CSV channel from a given file path.
# ```ballerina
# io:ReadableCSVChannel rCsvChannel = check io:openReadableCsvFile(srcFileName);
# ```
#
# + path - File path, which describes the location of the CSV
# + fieldSeparator - CSV record separator (i.e., comma or tab)
# + charset - Representation of the encoding characters in the file 
# + skipHeaders - Number of headers, which should be skipped
# + return - The `ReadableCSVChannel`, which could be used to iterate through the CSV records or else an `io:Error` if any error occurred.
public function openReadableCsvFile(@untainted string path,
                                    @untainted Separator fieldSeparator = ",",
                                    @untainted string charset = "UTF-8",
                                    @untainted int skipHeaders = 0) returns ReadableCSVChannel|Error {
    ReadableByteChannel byteChannel = check openReadableFile(path);
    ReadableCharacterChannel charChannel = new(byteChannel, charset);
    return new ReadableCSVChannel(charChannel, fieldSeparator, skipHeaders);
}

# Retrieves a writable CSV channel from a given file path.
# ```ballerina
# io:WritableCSVChannel wCsvChannel = check io:openWritableCsvFile(srcFileName);
# ```
# 
# + path - File path, which describes the location of the CSV
# + fieldSeparator - CSV record separator (i.e., comma or tab)
# + charset - Representation of the encoding characters in the file 
# + skipHeaders - Number of headers, which should be skipped
# + return - The `WritableCSVChannel`, which could be used to write the CSV records or else an `io:Error` if any error occurred
public function openWritableCsvFile(@untainted string path,
                                    @untainted Separator fieldSeparator = ",",
                                    @untainted string charset = "UTF-8",
                                    @untainted int skipHeaders = 0) returns WritableCSVChannel|Error {
    WritableByteChannel byteChannel = check openWritableFile(path);
    WritableCharacterChannel charChannel = new(byteChannel, charset);
    return new WritableCSVChannel(charChannel, fieldSeparator);
}
