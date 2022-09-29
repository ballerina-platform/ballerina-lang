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

function testByteArrayDowncastFromIntArray() returns boolean {
    byte b0 = 0;
    byte[] barr = [b0, 1, globalByte255];
    int[] iarr = barr;

    byte b178 = 178;
    iarr.push(b178);

    byte[] barr2 = <byte[]> iarr;

    return barr2.length() == 4 &&
            barr2[0] == 0 && barr2[1] == <byte> 1 && barr2[2] == globalByte255 && barr2[3] == b178;
}

function testBytesInIntArray() returns boolean {
    byte b0 = 0;
    byte b178 = 178;
    byte b255 = 255;

    int[] arr = [-1, b0, 120, b178, b255, 340];

    foreach [int, int] [index, val] in arr.enumerate() {
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

function testBytesInIntMap() returns boolean {
    byte b0 = 0;
    byte b130 = 130;
    byte b255 = 255;

    map<int> mp = {
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

function testByteStructuredTypeAsIntStructuredType() returns boolean {
    byte[] barr = [0, 1, 233, 255];
    int[] iarr = barr;
    iarr[iarr.length()] = 40;

    map<byte> bmap = {
        a: 0,
        b: 1,
        c: 254,
        d: 255
    };
    map<int> imap = bmap;
    imap["e"] = 230;

    return iarr[0] is byte && iarr[0] == 0 &&
            iarr[3] is byte && iarr[3] == <byte> 255 &&
            iarr[4] is byte && iarr[4] == 40 &&
            imap["a"] is byte && imap["a"] == 0 &&
            imap["d"] is byte && imap["d"] == 255 &&
            imap["e"] is byte && imap["e"] == 230;
}

function testInherentTypeViolationForArray() {
    byte[] barr = [0, 1, 233, 255];
    int[] iarr = barr;
    iarr[6] = 256;
}

function testInherentTypeViolationForMap() {
    map<byte> bmap = {
        a: 0,
        b: 123,
        c: 255
    };
    map<int> imap = bmap;
    imap["d"] = 300;
}

function testByteArrayCastToIntArray() {
    byte[4] barr = [0, 1, 2, 255];
    int[] iarr = <int[]> barr;
    int[4] iarr2 = <int[4]> barr;

    assertTrue(iarr is byte[]);
    assertTrue(iarr === barr);
    assertTrue(iarr2 is int[4]);
    assertTrue(iarr2 == barr);
    assertEquality(0, iarr[0]);
    assertEquality(1, iarr[1]);
    assertEquality(2, iarr[2]);
    assertEquality(255, iarr[3]);
}

function testDowncastOfByteArrayCastToIntArray() {
    byte[] barr = [0, 1, 2, 255];
    int[] iarr = <int[]> barr;
    byte[] barr2 = <byte[]> iarr;

    assertTrue(iarr === barr);
    assertTrue(iarr === barr2);
    assertTrue(barr === barr2);

    assertEquality(0, barr2[0]);
    assertEquality(1, barr2[1]);
    assertEquality(2, barr2[2]);
    assertEquality(255, barr2[3]);
}

function testByteArrayLiteralCastToReadOnlyType() {
    byte[] & readonly arr1 = <byte[] & readonly>base16 `aa bb`;
    assertTrue(arr1.isReadOnly());

    int[2] & readonly arr2 = <int[2] & readonly>base16 `aa bb`;
    assertTrue(arr2.isReadOnly());

    byte[2] & readonly arr3 = <readonly>base16 `aa bb`;
    assertTrue(arr3.isReadOnly());

    anydata[] & readonly arr4 = <anydata[] & readonly>base16 `aa bb`;
    assertTrue(arr4.isReadOnly());

    any[] arr5 = <any[] & readonly>base16 `aa bb`;
    byte[] arr6 = <byte[]> arr5;
    assertTrue(arr6.isReadOnly());
}

function testInherentTypeViolationOfByteArrayCastToIntArray() {
    final string expErrorReason = "{ballerina/lang.array}InherentTypeViolation";
    final string expErrorDetailMessage = "incompatible types: expected 'byte', found 'int'";

    byte[] barr = [0, 1, 2, 255];
    int[] iarr = <int[]> barr;

    var fn = function (int index, int value) {
        iarr[index] = value;
    };

    error? res = trap fn(0, -1);
    assertTrue(res is error);

    error err = <error> res;
    assertEquality(err.message(), expErrorReason);
    assertEquality(err.detail()["message"], expErrorDetailMessage);

    res = trap fn(2, 256);
    assertTrue(res is error);

    err = <error> res;
    assertEquality(err.message(), expErrorReason);
    assertEquality(err.detail()["message"], expErrorDetailMessage);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
