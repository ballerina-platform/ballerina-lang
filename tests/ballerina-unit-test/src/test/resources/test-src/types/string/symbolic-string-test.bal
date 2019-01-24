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

type Person object {
    public string name = "";
    public string fullName;
    function __init(string name = 'John, string firstname, string lastname = 'Doe) {
        self.name = name;
        self.fullName = firstname + " " + lastname;
    }
    public function getPersonInfo() returns string {
        return self.name + "-" + self.fullName;
    }
};

type Student record {
    string name;
    int age;
};

string var1 = 'abcd;

public string var2 = "";

public string var3 = 'efgh;

public string var4 = 'ijkl;

function contains() returns (boolean) {
    string source = 'HelloWorld;
    return source.contains("Hello");
}

function equalsIgnoreCaseTest1() returns (boolean) {
    string s1 = 'HelloWorld;
    string s2 = 'HelloWorld;
    return s1.equalsIgnoreCase(s2);
}

function equalsIgnoreCaseTest2() returns (boolean) {
    string s1 = 'HelloWorld;
    string s2 = 'lion;
    return s1.equalsIgnoreCase(s2);
}

function equalsIgnoreCaseTest3() returns (boolean) {
    string s1 = 'HelloWorld;
    string s2 = "HelloWorld";
    return s1.equalsIgnoreCase(s2);
}

function hasPrefix() returns (boolean) {
    string s = 'HelloWorld;
    string prefix = 'Hello;
    return s.hasPrefix(prefix);
}

function hasSuffix() returns (boolean) {
    string s = 'HelloWorld;
    string suffix = 'Hello;
    return s.hasSuffix(suffix);
}

function indexOf() returns (int) {
    string s = 'HelloWorld;
    string str = 'rld;
    return s.indexOf(str);
}

function lastIndexOf() returns (int) {
    string s = 'HelloWorld;
    string str = 'o;
    return s.lastIndexOf(str);
}

function replace() returns (string) {
    string s = 'HelloWorld;
    string source = 'Hello;
    string target = 'Bye;
    return s.replace(source, target);
}

function replaceAll() returns (string) {
    string s = 'HelloWorld;
    string source = "[o]+";
    string target = "0";
    return s.replaceAll(source, target);
}

function substring() returns (string) {
    string s = 'HelloWorld;
    return s.substring(0, 4);
}

function toLower() returns (string) {
    string s = 'HelloWorld;
    return s.toLower();
}

function toUpper() returns (string) {
    string s = 'HelloWorld;
    return s.toUpper();
}

function stringValueOf() returns (string) {
    string s = 'HelloWorld;
    return <string>(s);
}

function lengthOfStr() returns (int) {
    string s = 'HelloWorld;
    return s.length();
}

function unescape() returns (string) {
    string s = 'HelloWorld;
    return s.unescape();
}

function toByteArray() returns (byte[]) {
    string s = 'HelloWorld;
    return s.toByteArray("UTF-8");
}

function testSplit() returns (string[]) {
    string j = 'hellomellotello;
    string k = 'o;
    return j.split(k);
}

function testStringArray() returns (string) {
    string [] stringArr = ['hello, 'world];
    return stringArr[0];
}

function testMap() returns (string[]) {
    map<any> addrMap = { number: "No.20", road: 'Mount_Lavinia, country: 'SriLanka };
    addrMap['postalCode] = 'PO00300;
    string [3] stringArr = [<string>addrMap['country], <string>addrMap['road], <string>addrMap["postalCode"]];
    return stringArr;
}

function testTuples() returns (int, string, string) {
    return (10, 'John_Snow, 'UK);
}

function testJson() returns (json) {
    json j1 = 'Apple;
    json j2 = { name: 'apple, color: 'red};

    int age = 30;
    json p = { fname: 'John, lname: 'Stallone, "age": age };

    json firstName = p.fname;
    json lastName = p['lname];
    p['age] = 66;
    json newAge = p['age];

    json p2 = {
        fname: 'Peter,
        lname: 'Stallone,
        'age: age,
        address: {
            line: "20 Palm Grove",
            city: "Colombo 03",
            country: 'SriLanka
        }
    };
    p2.address.province = 'Western;
    json finalJsonArr = [j1, j2, p, firstName, lastName, newAge, p2];
    return finalJsonArr;
}

function testObject() returns (json) {
    Person p1 = new('John);
    Person p2 = new('Adam, name = 'Adam, lastname = 'Page);
    json j = [p1.getPersonInfo(), p2.getPersonInfo()];
    return j;
}

function testRecords() returns (string) {
    Student stu = { name: 'AdamPage, age: 17};
    var value = stu['name];
    if (value is string) {
        return value;
    }
    return "empty";
}

function testGlobalVars() returns (string) {
   string str = var1 + var3 + 'mnop123 + var4;
   var2 = str + var2;
   return var2;
}

function testConditions() returns (boolean) {
    string str = var1 + 'hello123 + var3;
    if (str == 'abcdhello123efgh) {
        return true;
    }
    return false;
}

function testUnicode_1() returns (boolean) {
    string str = 'බැලරිනා;
    if (str == 'බැලරිනා) {
        return true;
    }
    return false;
}

function testUnicode_2() returns (boolean) {
    string str = '⺑⺺⻨⼚⾉⼚⽫;
    if (str == '⺑⺺⻨⼚⾉⼚⽫) {
        return true;
    }
    return false;
}

function testConcatenation() returns (string) {
    string str = "hello" + 'world;
    return str;
}

function testUnicodeEquality() returns (boolean) {
    string str = 'බැලරිනා;
    if (str == "බැලරිනා") {
        return true;
    }
    return false;
}
