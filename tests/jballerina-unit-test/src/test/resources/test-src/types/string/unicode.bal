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

const ASSERTION_ERROR_REASON = "AssertionError";

function testUnicode() {
    string s1 = "\u{0633}";
    string s2 = "\u{633}";
    string s3 = "ABC\u{0644}\u{1048}CDE";
    string s4 = "ABC\u{644}\u{1048}CDE";
    string s5 = "ABC \u{0633} CDE";
    string s6 = "ABC \u{0633} CDE \u{0644} DEF \u{0644} XYZ";
    string s7 = "\u{1F600}";
    string s8 = "\u{1F600}\u{1F610}\u{1D702}Bar";
    string s9 = "\u{41}";
    string s10 = "\u{43}\u{061}\u{000074}";

    assertEquality(s1, "س");
    assertEquality(s2, "س");
    assertEquality(s3, "ABCل၈CDE");
    assertEquality(s4, "ABCل၈CDE");
    assertEquality(s5, "ABC س CDE");
    assertEquality(s6, "ABC س CDE ل DEF ل XYZ");
    assertEquality(s7, "😀");
    assertEquality(s8, "😀😐𝜂Bar");
    assertEquality(s9, "A");
    assertEquality(s10, "Cat");

    byte[] bArray = s9.toBytes();
    assertEquality(s7.length(), 1);
    assertEquality(bArray.length(), 1);
    assertEquality(bArray.toString(), "[65]");

    // Test NumericEscape Escaping
    string s11 = "\\u{61}";
    string s12 = "\\\u{61}";
    string s13 = "\\\\u{61}";
    string s14 = "\\\\\u{61}pp\\\u{6C}e";
    string s15 = "\\\\u{61}pp\\\u{6C}e";
    string s16 = "\\\\u{61}pp\\\\u{6C}e";
    string s17 = "A\\u{61}\u{42}BE";
    string s18 = "\\" + "u{61}";
    string s19 = "\\u{D800}"; // no error for \u{D800} as it is escaped
    
    assertEquality(s11, "\\u{61}");
    assertEquality(s12, "\\a");
    assertEquality(s13, "\\\\u{61}");
    assertEquality(s14, "\\\\app\\le");
    assertEquality(s15, "\\\\u{61}pp\\le");
    assertEquality(s16, "\\\\u{61}pp\\\\u{6C}e");
    assertEquality(s17, "A\\u{61}BBE");
    assertEquality(s18, s11);
    assertEquality(s19, "\\u{D800}");
}

function assertEquality(anydata actual, anydata expected) {
    if (actual == expected) {
        return;
    }
    
    panic error(ASSERTION_ERROR_REASON,
                 message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
