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

byte globalByte0 = 0;
byte globalByte30 = 30;
byte globalByte255 = 255;

function testByteAsInt() returns boolean {
    byte b0 = 0;
    int i0 = b0;
    int ig0 = globalByte0;
    boolean byte0AsInt = i0 == ig0 && i0 == 0;

    byte b30 = 30;
    int i30 = takeAnInt(b30);
    int ig30 = globalByte30;
    boolean byte30AsInt = i30 == ig30 && i30 == 30;

    byte b255 = 255;
    int i255 = b255;
    int ig255 = takeAnInt(globalByte255);
    boolean byte255AsInt = i255 == ig255 && i255 == 255;

    return byte0AsInt && byte30AsInt && byte255AsInt;
}

function takeAnInt(int i) returns int {
    return i;
}

function testByteDowncastFromInt() returns boolean {
    int i0 = globalByte0;
    byte b0 = <byte> i0;

    byte bl178 = 178;
    int i178 = bl178;
    byte b178 = <byte> i178;

    int i255 = globalByte255;
    byte b255 = <byte> i255;

    return b0 == 0 && b178 == 178 && b255 == 255;
}
