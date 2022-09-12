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

import ballerina/lang.regexp;
import ballerina/jballerina.java;

function testFind() {
//    var res = regexp:find("a","abca");
//    print(res);

//    var res = regexp:findGroups("\\G(\\w+)=(\\w+);","name1=gil;name2=orit;", 10);
//Start index: 10 End index: 21 Found: name2=orit;
//    var res = regexp:findGroups("([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?","14:35:59.99");
    var res = regexp:findAllGroups("(ab)(cd)","abcdefabcdef");

//    var res = regexp:matchAt("\\G(\\w+)=(\\w+);","name1=gil;name2=orit;", 10);
//    var res = regexp:matchAt("\\d\\d\\d","a123", 1);

//var res = regexp:matchGroupsAt("([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?","14:35:59.99");
//Start index: 0 End index: 11 Found: 14:35:59.99
//Start index: 0 End index: 2 Found: 14
//Start index: 3 End index: 5 Found: 35
//Start index: 6 End index: 8 Found: 59

//    var res = regexp:isFullMatch("\\d\\d\\d","a123");

//var res = regexp:fullMatchGroups("([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?","14:35:59.99");
//Start index: 0 End index: 11 Found: 14:35:59.99
//Start index: 0 End index: 2 Found: 14
//Start index: 3 End index: 5 Found: 35
//Start index: 6 End index: 8 Found: 59
//var res = regexp:fullMatchGroups("([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1,3}))?","a14:35:59.99");
//res is nil

//var res = regexp:replace("0+","10010011", "*");
    print(res);
}







public function print(any|error value) = @java:Method {
    'class: "org.ballerinalang.langlib.test.LangLibRegexTest",
    name: "print"
} external;
