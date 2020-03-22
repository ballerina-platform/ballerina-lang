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

# Retrieves a ReadableByteChannel from a given file path.
#
# + path - Relative/absolute path string to locate the file
# + return - ByteChannel representation of the file resource or `Error` if any error occurred
public function openReadableFile(@untainted string path) returns @tainted ReadableByteChannel|Error {
    return openReadableFileExtern(java:fromString(path));
}

function openReadableFileExtern(@untainted handle path) returns @tainted ReadableByteChannel|Error = @java:Method {
    name: "openReadableFile",
    class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

# Retrieves a WritableByteChannel from a given file path.
#
# + path - Relative/absolute path string to locate the file
# + append - Append to end of file.
# + return - ByteChannel representation of the file resource or `Error` if any error occurred
public function openWritableFile(@untainted string path, boolean append = false)
    returns @tainted WritableByteChannel|Error {
    return openWritableFileExtern(java:fromString(path), append);
}

function openWritableFileExtern(@untainted handle path, boolean append)
    returns @tainted WritableByteChannel|Error = @java:Method {
    name: "openWritableFile",
    class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

# Creates an in-memory channel which will reference stream of bytes.
#
# + content - Content which should be exposed as channel
# + return - ByteChannel representation to read the memory content or `Error` if any error occurred
public function createReadableChannel(byte[] content) returns ReadableByteChannel|Error {
    return createReadableChannelExtern(content);
}

function createReadableChannelExtern(byte[] content) returns ReadableByteChannel|Error = @java:Method {
    name: "createReadableChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.ByteChannelUtils"
} external;

# Retrieves a readable CSV channel from a give file path.
#
# + path - File path which describes the location of the CSV
# + fieldSeparator - CSV record separator (i.e comma or tab)
# + charset - Encoding characters in the file represents
# + skipHeaders - Number of headers which should be skipped
# + return - ReadableCSVChannel which could be used to iterate through the CSV records
public function openReadableCsvFile(@untainted string path,
                            @untainted public Separator fieldSeparator = ",",
                            @untainted public string charset = "UTF-8",
                            @untainted public int skipHeaders = 0) returns @tainted ReadableCSVChannel|Error {
    ReadableByteChannel byteChannel = check openReadableFile(path);
    ReadableCharacterChannel charChannel = new(byteChannel, charset);
    return new ReadableCSVChannel(charChannel, fieldSeparator, skipHeaders);
}

# Retrieves a writable CSV channel from a give file path.
#
# + path - File path which describes the location of the CSV
# + fieldSeparator - CSV record separator (i.e comma or tab)
# + charset - Encoding characters in the file represents
# + skipHeaders - Number of headers which should be skipped
# + return - WritableCSVChannel which could be used to write CSV records or `Error` if any error occurred
public function openWritableCsvFile(@untainted string path,
                                    @untainted public Separator fieldSeparator = ",",
                                    @untainted public string charset = "UTF-8",
                                    @untainted public int skipHeaders = 0) returns @tainted WritableCSVChannel|Error {
    WritableByteChannel byteChannel = check openWritableFile(path);
    WritableCharacterChannel charChannel = new(byteChannel, charset);
    return new WritableCSVChannel(charChannel, fieldSeparator);
}
