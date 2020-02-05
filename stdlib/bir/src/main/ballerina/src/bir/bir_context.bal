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

// TODO Refactor following methods
public function populateBIRModuleFromBinary(byte[] modBinary, boolean symbolsOnly) returns Package {
    io:ReadableByteChannel byteChannel = checkpanic io:createReadableChannel(modBinary);
    ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    ConstPoolParser cpParser = new(reader);
    var cp = cpParser.parse();
    BirChannelReader birReader = new(reader, cp);
    PackageParser pkgParser = new(birReader, symbolsOnly);
    Package mod = pkgParser.parsePackage();
    return <@untainted Package> mod;
}

function checkValidBirChannel(ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = error( "Invalid BIR binary content, unexptected header" );
        panic err;
    }
}

function checkVersion(ChannelReader reader) {
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

