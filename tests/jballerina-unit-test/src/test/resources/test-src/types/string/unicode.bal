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

    if (s1 == "س" && s2 == "س" && s3 == "ABCل၈CDE" && s4 == "ABCل၈CDE" && s5 == "ABC س CDE"
            && s6 == "ABC س CDE ل DEF ل XYZ") {
        return;
    }
    panic error(ASSERTION_ERROR_REASON, message = "expected 'س', 'س', 'ABCل၈CDE', 'ABCل၈CDE', 'ABC س CDE', "
            + " 'ABC س CDE ل DEF ل XYZ', found " + s1 + "', '" + s2 + "', '" + s3 + "', '" + s4 + "', '"
            + s5 + "', '" + s6 + "'");
}
