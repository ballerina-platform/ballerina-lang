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

public enum Color {
  RED,
  GREEN,
  BLUE
}

public enum Language {
    ENG = "English",
    TL = "Tamil",
    SI = "Sinhala"
}

public enum Bands {
    QUEEN,
    PF = "Pink " + "Floyd"
}

string colorOfSky = BLUE;
string globalLang = ENG;
string rockBand = PF;

function testBasicEnumSupport() {
    assert(RED, "RED");
    assert(ENG, "English");
    assert(PF, "Pink Floyd");

    string a = RED;
    assert(a, "RED");

    string si = SI;
    assert(si, "Sinhala");

    string q = QUEEN;
    assert(q, "QUEEN");
}

function testEnumAsType() {
    Color c = RED;
    assert(c, "RED");

    Language l = "Tamil";
    assert(l, "Tamil");

    PF pf = "Pink Floyd";
    assert(pf, "Pink Floyd");
}

function testEnumAsGlobalRef() {
    assert(colorOfSky, "BLUE");
    assert(globalLang, "English");
    assert(rockBand, "Pink Floyd");
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}
