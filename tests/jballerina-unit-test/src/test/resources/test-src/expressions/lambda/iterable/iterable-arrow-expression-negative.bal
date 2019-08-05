// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testSingleIterable() {

    map<string> words = { a: "ant", b: "bear"};
    float[] flArray = [4.0, 5.0, 6.0, 7.0];

    int[] lambda2 = words.map(word => word[1].toUpperAscii()); // incompatible types: expected 'int[]', found 'map<string>'
    string[] lambda3 = words.map((index, word) => word[1].toUpperAscii()); // invalid number of parameters used in arrow expression. expected: '1' but found '2'

    int avg1 = flArray.map((index, value) => value).length(); // invalid number of parameters used in arrow expression. expected: '1' but found '2'
    string avg2 = flArray.map(entry => entry).length(); // incompatible types: expected 'int', found 'int'
    string avg3 = flArray.map(entry => entry.toUpperAscii()).length(); // undefined function 'toUpperAscii'
    string str1 = flArray.map(entry => entry.toString()); // incompatible types: expected 'string', found 'string[]'
}

function testChainedIterable() {
    map<string> words = { a: "ant", b: "bear"};
    float[] flArray = [4.0, 5.0, 6.0, 7.0];
    string[] lambda1 = words.filter(word => word[1] == "ant").map(word => word.toUpperAscii() + " MAN"); // incompatible types: expected 'string[]', found 'map<string>'
    string[] str = words.filter(word => word[1] == "ant").map(word => word[1].toUpperAscii() + " MAN").length(); // incompatible types: expected 'string[]', found 'int'
}
