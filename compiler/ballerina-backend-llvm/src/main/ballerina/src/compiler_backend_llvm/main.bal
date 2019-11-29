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
import ballerina/bir;
import ballerina/stringutils;

public function main(string... args) {
    string pathToEntryBir = <@untainted> args[0];
    string targetPath = args[1];
    boolean dumpLLVM = stringutils:equalsIgnoreCase(args[2], "true");
    boolean noOptimize = stringutils:equalsIgnoreCase(args[3], "true");
    generateObjFile(pathToEntryBir, targetPath, dumpLLVM, noOptimize);
}

function generateObjFile(string pathToEntryBir, string targetPath, boolean dumpLLVM, boolean noOptimize) {
    byte[] birBinary = readFileFully(pathToEntryBir);
    io:ReadableByteChannel byteChannel = checkpanic io:createReadableChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    bir:ConstPoolParser cpParser = new(reader);
    bir:BirChannelReader birReader = new(reader, cpParser.parse());
    bir:PackageParser pkgParser = new(birReader, false);
    genPackage(pkgParser.parsePackage(), targetPath, dumpLLVM, noOptimize);
}

function checkValidBirChannel(bir:ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(bir:ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = error( "Invalid BIR binary content, unexptected header" );
        panic err;
    }
}

function checkVersion(bir:ChannelReader reader) {
    var birVersion = reader.readInt32();
    var supportedBirVersion = 51;
    if (birVersion != supportedBirVersion){
        error err = error( "Unsupported BIR version " + birVersion.toString() + ", supports version " +
                            supportedBirVersion.toString());
        panic err;
    }
}

function arrayEq(byte[] x, byte[] y) returns boolean {
    var xLen = x.length();

    if xLen != y.length() {
        return false;
    }

    int i = 0;
    while i < xLen {
        if (x[i] != y[i]){
            return false;
        }
        i = i + 1;
    }
    return true;
}
