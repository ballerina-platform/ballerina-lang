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

function testUnicodeNegative() {
    string s1 = "\u{D800}";
    string s2 = "\u{D8FF}";
    string s3 = "\u{DFFF}";
    string s4 = "\u{11FFFF}";
    string s5 = "\u{12FFFF}";
    string s6 = "\u{DFFF}\u{DAFF}";
    string s7 = "\u{12FFFF} ABC \u{DFFF} DEF \u{DAFF}";
    string s7 = "\u{12FFFF} ABC \u{DFFFAAA} DEF \u{FFFFFFF}";
}
