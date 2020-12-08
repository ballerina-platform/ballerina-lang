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

public function benchmarkStringEqualsIgnoreCase() {
    string test = "testString";
    boolean result = test.equalsIgnoreCaseAscii("ram");
}

public function benchmarkStringConcat() {
    string s1 = "John";
    string s2 = "Doe";
    string s3 = s2 + s2;
}

public function benchmarkStringHasPrefix() {
    string name = "randomPerson";
    string prefix = "Mr";
    boolean result = name.startsWith(prefix);
}

public function benchmarkStringHasSuffix() {
    string name = "randomPerson";
    string suffix = "Darwin";
    boolean result = name.endsWith(suffix);
}

public function benchmarkStringIndexOf() {
    string s = "randomPerson";
    string str = "Darwin";
    int? result = s.indexOf(str);
}

public function benchmarkStringLastIndexOf() {
    string s = "randomPersonDarwincy";
    string str = "Darwin";
    int? result = s.lastIndexOf(str);
}

public function benchmarkStringSubstring() {
    string s = "This statment is to test substring function.";
    string str = s.substring(5, 16);
}

public function benchmarkStringToLower() {
    string s = "CONVERT TO LOWER CASE";
    string str = s.toLowerAscii();
}

public function benchmarkStringToUpper() {
    string s = "convert to upper case";
    string str = s.toUpperAscii();
}

public function benchmarkStringTrim() {
    string s = " trim ";
    string str = s.trim();
}

public function benchmarkStringIntValueOf() {
    string five = (5).toString();
}

public function benchmarkStringFloatValueOf() {
    string floatNumber = (777.777).toString();
}

public function benchmarkStringBooleanValueOf() {
    string strTrue = (true).toString();
}

public function benchmarkStringValueOf() {
    string str = <string>("This is string.");
}

public function benchmarkStringJsonValueOf() {
    json testJson = { "Hello": "World" };
    string str = testJson.toString();
}

public function benchmarkStringLength() {
    string str = "this is a test string";
    int i = str.length();
}

