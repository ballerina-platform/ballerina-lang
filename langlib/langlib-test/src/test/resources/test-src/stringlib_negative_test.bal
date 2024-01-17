// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const string constString1 = "abc";
const string constString6 = "#";

string & readonly roString1 = "--121";
string & readonly roString2 = "+-121";

string:Char char1 = "\u{0051}";
string:Char char2 = "\u{002b}";

string:Char & readonly roChar = "\u{002b}";

function getString() returns string {
    return "-987";
}

function testPadStart() {
    string _ = "abc".padStart();
    string _ = "abc".padStart(100, "");
    string _ = "abc".padStart(100, "  ");
    int _ = "abc".padStart(100);

    string _ = "abc".padStart(100, constString6);
    byte _ = "abc".padStart(100, roChar);
    string:Char _ = char1.padStart(100, char2);
    string:Char _ = roString1.padStart(100, roString2);

    string _ = "abc".padStart(100f);
    string _ = "abc".padStart(100d);
    string _ = roString2.padStart(0x1.1a, ".");
    string _ = "abc".padStart("0");

    string _ = "abc".padStart(12, getString());
}

function testPadEnd() {
    string _ = "abc".padEnd();
    string _ = "abc".padEnd(100, "");
    string _ = "abc".padEnd(100, "  ");
    int _ = "abc".padEnd(100);

    string _ = "abc".padEnd(100, constString6);
    byte _ = "abc".padEnd(100, roChar);
    string:Char _ = char1.padEnd(100, char2);
    string:Char _ = roString1.padEnd(100, roString2);

    string _ = "abc".padEnd(100f);
    string _ = "abc".padEnd(100d);
    string _ = roString2.padEnd(0x1.1a, ".");
    string _ = "abc".padEnd("0");

    string _ = "abc".padEnd(12, getString());
}

function testPadZero() {
    string _ = "-abc".padZero();
    string _ = "++abc".padZero(100, "");
    string _ = "  -abc".padZero(100, "  ");
    int _ = "abc".padZero(100);

    string _ = "abc".padZero(100, constString6);
    byte _ = "abc".padZero(100, roChar);
    string:Char _ = char1.padZero(100, char2);
    string:Char _ = roString1.padZero(100, roString2);

    string _ = "abc".padZero(100f);
    string _ = "abc".padZero(100d);
    string _ = roString2.padZero(0x1.1a, ".");
    string _ = "abc".padZero("0");

    string _ = "abc".padZero(12, getString());
}
