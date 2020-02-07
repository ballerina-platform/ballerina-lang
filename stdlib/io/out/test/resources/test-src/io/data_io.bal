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

import ballerina/io;

function testWriteFixedSignedInt(int value, string path, io:ByteOrder byteOrder) returns @tainted io:Error? {
    io:WritableByteChannel ch = check io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, byteOrder);
    var result = dataChannel.writeInt64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedSignedInt(string path, io:ByteOrder byteOrder) returns @tainted int|error {
    var ch = io:openReadableFile(path);
    if (ch is io:ReadableByteChannel) {
        io:ReadableDataChannel dataChannel = new(ch, byteOrder);
        int result = check dataChannel.readInt64();
        var closeResult = dataChannel.close();
        return result;
    } else {
        return ch;
    }
}

function testWriteVarInt(int value, string path, io:ByteOrder byteOrder) returns @tainted io:Error? {
    io:WritableByteChannel ch = check io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, byteOrder);
    var result = dataChannel.writeVarInt(value);
    var closeResult = dataChannel.close();
}

function testReadVarInt(string path, io:ByteOrder byteOrder) returns @tainted int|io:Error {
    var ch = io:openReadableFile(path);
    if (ch is io:ReadableByteChannel) {
        io:ReadableDataChannel dataChannel = new(ch, byteOrder);
        int result = check dataChannel.readVarInt();
        var closeResult = dataChannel.close();
        return result;
    } else {
        return ch;
    }
}

function testWriteFixedFloat(float value, string path, io:ByteOrder byteOrder) returns @tainted io:Error? {
    io:WritableByteChannel ch = check io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, byteOrder);
    var result = dataChannel.writeFloat64(value);
    var closeResult = dataChannel.close();
}

function testReadFixedFloat(string path, io:ByteOrder byteOrder) returns @tainted float|io:Error {
    var ch = io:openReadableFile(path);
    if (ch is io:ReadableByteChannel) {
        io:ReadableDataChannel dataChannel = new(ch, byteOrder);
        float result = check dataChannel.readFloat64();
        var closeResult = dataChannel.close();
        return result;
    } else {
        return ch;
    }
}

function testWriteBool(boolean value, string path, io:ByteOrder byteOrder) returns @tainted io:Error? {
    io:WritableByteChannel ch = check io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, byteOrder);
    var result = dataChannel.writeBool(value);
    var closeResult = dataChannel.close();
}

function testReadBool(string path, io:ByteOrder byteOrder) returns @tainted boolean|io:Error {
    var ch = io:openReadableFile(path);
    if (ch is io:ReadableByteChannel) {
        io:ReadableDataChannel dataChannel = new(ch, byteOrder);
        boolean result = check dataChannel.readBool();
        var closeResult = dataChannel.close();
        return result;
    } else {
        return ch;
    }
}

function testWriteString(string path, string content, string encoding, io:ByteOrder byteOrder) returns @tainted io:Error? {
    io:WritableByteChannel ch = check io:openWritableFile(path);
    io:WritableDataChannel dataChannel = new(ch, byteOrder);
    var result = check dataChannel.writeString(content, encoding);
    var closeResult = dataChannel.close();
    return result;
}

function testReadString(string path, int nBytes, string encoding, io:ByteOrder byteOrder) returns @tainted string|io:Error {
    var ch = io:openReadableFile(path);
    if (ch is io:ReadableByteChannel) {
        io:ReadableDataChannel dataChannel = new(ch, byteOrder);
        string result = check dataChannel.readString(nBytes, encoding);
        var closeResult = dataChannel.close();
        return result;
    } else {
        return ch;
    }
}
