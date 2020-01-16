// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testByteAsJson() returns boolean {
    byte b0 = 0;
    json j0 = b0;
    json ig0 = globalByte0;
    boolean byte0AsJson = j0 == ig0 && j0 == 0;

    byte b30 = 30;
    json j30 = takeAJson(b30);
    json jg30 = globalByte30;
    boolean byte30AsJson = j30 == jg30 && j30 == 30;

    byte b255 = 255;
    json j255 = b255;
    json jg255 = takeAJson(globalByte255);
    boolean byte255AsJson = j255 == jg255 && j255 == 255;

    return byte0AsJson && byte30AsJson && byte255AsJson;
}

function takeAJson(json j) returns json {
    return j;
}

function testByteDowncastFromJson() returns boolean {
    json j0 = globalByte0;
    byte b0 = <byte> j0;

    byte bl178 = 178;
    json j178 = bl178;
    byte b178 = <byte> j178;

    json j255 = globalByte255;
    byte b255 = <byte> j255;

    return b0 == 0 && b178 == 178 && b255 == 255;
}

function testByteArrayDowncastFromJsonArray() returns boolean {
    byte b0 = 0;
    byte[] barr = [b0, 1, globalByte255];
    json[] jarr = barr;

    byte b178 = 178;
    jarr.push(b178);

    byte[] barr2 = <byte[]> jarr;

    return barr2.length() == 4 &&
            barr2[0] == 0 && barr2[1] == <byte> 1 && barr2[2] == globalByte255 && barr2[3] == b178;
}

function testBytesInJsonArray() returns boolean {
    byte b0 = 0;
    byte b178 = 178;
    byte b255 = 255;

    json[] arr = [-1, b0, 120, b178, b255, 340];

    foreach [int, json] [index, val] in arr.enumerate() {
        match index {
            1 => {
                if val != b0 {
                    return false;
                }
            }
            3 => {
                if val != 178 {
                    return false;
                }
            }
            4 => {
                if val != b255 {
                    return false;
                }
            }
            _ => {
                if val != arr[index] {
                    return false;
                }
            }
        }
    }
    return true;
}

function testBytesInJsonMap() returns boolean {
    byte b0 = 0;
    byte b130 = 130;
    byte b255 = 255;

    map<json> mp = {
        a: -1,
        b: b0,
        c: 121,
        d: b130,
        e: b255,
        f: 300
    };

    return mp["a"] == -1 && mp["b"] == b0 && mp["c"] == 121 &&
               mp["d"] == 130 && mp["e"] == b255 && mp["f"] == 300;
}

function testByteStructuredTypeAsJsonStructuredType() returns boolean {
    byte[] barr = [0, 1, 233, 255];
    json[] jarr = barr;
    jarr[jarr.length()] = 40;

    map<byte> bmap = {
        a: 0,
        b: 1,
        c: 254,
        d: 255
    };
    map<json> jmap = bmap;
    jmap["e"] = 230;

    return jarr[0] is byte && jarr[0] == 0 &&
            jarr[3] is byte && jarr[3] == <byte> 255 &&
            jarr[4] is byte && jarr[4] == 40 &&
            jmap["a"] is byte && jmap["a"] == 0 &&
            jmap["d"] is byte && jmap["d"] == 255 &&
            jmap["e"] is byte && jmap["e"] == 230;
}

function testInherentTypeViolationForArray() {
    byte[] barr = [0, 1, 233, 255];
    json[] jarr = barr;
    jarr[6] = 256;
}

function testInherentTypeViolationForMap() {
    map<byte> bmap = {
        a: 0,
        b: 123,
        c: 255
    };
    map<json> jmap = bmap;
    jmap["d"] = 300;
}
