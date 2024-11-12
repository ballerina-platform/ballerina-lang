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

import ballerina/lang.'int as ints;
import ballerina/lang.'string as strings;
import ballerina/lang.'value as values;

type UndergradStudent record {|
    readonly int id;
    readonly string name;
    int grade;
|};

public class Student {
    string name;
    string school;

    public function init(string name, string school) {
        self.name = name;
        self.school = school;
    }

    public function getDetails() returns string {
        return self.name + " from " + self.school;
    }
}

public class Teacher {
    string name;
    string school;

    public function init(string name, string school) {
        self.name = name;
        self.school = school;
    }

    public function getDetails() returns string {
        return self.name + " from " + self.school;
    }

    public function toString() returns string {
        return self.getDetails();
    }
}

function testIntValueFromBalString() {
    int x1 = 12;
    ints:Signed32 x2 = 2147483647;
    ints:Signed16 x3 = -32768;
    ints:Signed8 x4 = 100;
    ints:Unsigned32 x5 = 4294967295;
    ints:Unsigned16 x6 = 450;
    ints:Unsigned8 x7 = 221;
    byte x8 = 10;

    string s1 = "12";
    string s2 = "2147483647";
    string s3 = "-32768";
    string s4 = "100";
    string s5 = "4294967295";
    string s6 = "450";
    string s7 = "221";
    string s8 = "10";

    assert(s1.fromBalString(), x1);
    assert(s2.fromBalString(), x2);
    assert(s3.fromBalString(), x3);
    assert(s4.fromBalString(), x4);
    assert(s5.fromBalString(), x5);
    assert(s6.fromBalString(), x6);
    assert(s7.fromBalString(), x7);
    assert(s8.fromBalString(), x8);
}

function testStringValueFromBalString() {
    string a = "Anne";
    strings:Char b = "m";
    string c = "";

    string aa = a.toBalString();
    string bb = b.toBalString();
    string cc = c.toBalString();

    assert(aa.fromBalString(), a);
    assert(bb.fromBalString(), b);
    assert(cc.fromBalString(), c);
}

function testFloatingPointNumbersFromBalString() {
    float x1 = 12.342;
    float x2 = 0.0/0.0;
    float x3 = 4.0/0.0;
    decimal x4 = 345.2425341;
    decimal x5 = 1;

    string s1 = "12.342";
    string s2 = "float:NaN";
    string s3 = "float:Infinity";
    string s4 = "345.2425341d";
    string s5 = "1d";

    assert(s1.fromBalString(), x1);
    assert(s2.fromBalString(), x2);
    assert(s3.fromBalString(), x3);
    assert(s4.fromBalString(), x4);
    assert(s5.fromBalString(), x5);
}

function testAnydataNilFromBalString() {
    anydata a = 99;
    () b = ();

    string s1 = "99";
    string s2 = "()";

    assert(s1.fromBalString(), a);
    assert(s2.fromBalString(), b);
}

function testMapFromBalString() {
    map<string> mapVal1 = {"name":"ABC", "school":"City College"};
    map<anydata|error> mapVal2 = {"1":12, "2":"James", "3":{"x":"AA","y":(1.0/0.0)}, "4":(),
            "5":error("Failed to get account balance", details = true)};
    map<int> mapVal3 = {};

    string s1 = "{\"name\":\"ABC\",\"school\":\"City College\"}";
    string s2 = "{\"1\":12,\"2\":\"James\",\"3\":{\"x\":\"AA\",\"y\":float:Infinity},\"4\":()}";
    string s3 = "{}";

    anydata|error result1 = s1.fromBalString();
    anydata|error result2 = s2.fromBalString();
    anydata|error result3 = s3.fromBalString();

    if (result1 is map<string>) {
        assert(result1, mapVal1);
        assert(result1.keys(), ["name","school"]);
        string str = "";
        result1.forEach(function (string val) {
            str += val;
        });
        assert(str, "ABCCity College");
    }

    if (result2 is map<anydata>) {
        assert(result2.keys(), ["1","2","3","4"]);
        assert(result2.get("1"), mapVal2.get("1"));
        assert(result2.get("2"), mapVal2.get("2"));
        assert(result2.get("3"), mapVal2.get("3"));
        assert(result2.get("4"), mapVal2.get("4"));
    }

    assert(result3, mapVal3);

    string mapExpString = string `{"name":"John","city":"London"}`;
    anydata|error output = mapExpString.fromBalString();
    assert(output is error, false);
    if (output is anydata) {
        assert(output, {"name": "John", "city": "London"});
    }

    string mapExpString1 = string `{"key1": [ 1, 2, "three", ["",4,{"key1":"val1", "key2": 2}]],  "key2"  : `;
    string mapExpString2 = string `{"key1": [1, 2, "three", ["",4,{"key1":"val1", "key2": 2}]],  "key2": "London"}}`;
    mapExpString = mapExpString1 + mapExpString2;
    output = mapExpString.fromBalString();
    assert(output is error, false);
    if (output is anydata) {
        assert(output, {"key1":[1,2,"three",["",4,{"key1":"val1","key2":2}]],
        "key2":{"key1":[1,2,"three",["",4,{"key1":"val1","key2":2}]],"key2":"London"}});
    }

    mapExpString = string `{"name":"John",city:"London"}`;
    output = mapExpString.fromBalString();
    assert(output is error, true);
    if (output is error) {
        assert("invalid expression style string value: the map keys are not enclosed with '\"'.",
        <string>checkpanic output.detail()["message"]);
    }

    mapExpString = string `{name:"John",city:"London"}`;
    output = mapExpString.fromBalString();
    assert(output is error, true);
    if (output is error) {
        assert("invalid expression style string value: the map keys are not enclosed with '\"'.",
        <string>checkpanic output.detail()["message"]);
    }

    mapExpString = string `{"   name    ":"                John","     city ":"        London                    "}`;
    output = mapExpString.fromBalString();
    assert(output is error, false);
    if (output is anydata) {
        assert(output, {"   name    ": "                John", "     city ": "        London                    "});
    }

    mapExpString = string `   {"name" :    "John"  ,  "city"  :  "London"}   `;
    output = mapExpString.fromBalString();
    assert(output is error, false);
    if (output is anydata) {
        assert(output, {"name": "John", "city": "London"});
    }

    mapExpString = string `
              {                 "name"  :
                "John" ,
                         "city" : "London"      }`;
    output = mapExpString.fromBalString();
    assert(output is error, false);
    if (output is anydata) {
        assert(output, {"name": "John", "city": "London"});
    }

    mapExpString = string `
              {                 "intval"  :
                3 ,
                         "floatval" : 12.55      }`;
    output = mapExpString.fromBalString();
    assert(output is error, false);
    if (output is anydata) {
        assert(output, {intval: 3, floatval: 12.55});
    }

    mapExpString = string `
              {                 "name"  :
                "John" ,
                         city : "London"      }`;
    output = mapExpString.fromBalString();
    assert(output is error, true);
    if (output is error) {
        assert("invalid expression style string value: the map keys are not enclosed with '\"'.",
        <string>checkpanic output.detail()["message"]);
    }

    mapExpString = string `
              {                 "name"  :
                3 ,
                         city" : 12.55      }`;
    output = mapExpString.fromBalString();
    assert(output is error, true);
    if (output is error) {
        assert("invalid expression style string value: the map keys are not enclosed with '\"'.",
        <string>checkpanic output.detail()["message"]);
    }

    mapExpString = string `
              {                 name"  :
                3 ,
                         city" : 12.55      }`;
    output = mapExpString.fromBalString();
    assert(output is error, true);
    if (output is error) {
        assert("invalid expression style string value: the map keys are not enclosed with '\"'.",
        <string>checkpanic output.detail()["message"]);
    }
}

function testTableFromBalString() {
    string s1 = "table key(name) [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
                      "{\"id\":2,\"name\":\"John\",\"grade\":13}]";
    string s2 = "table key() [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
                       "{\"id\":2,\"name\":\"John\",\"grade\":13}]";
    string s3 = "table key(id,name) []";
    table<UndergradStudent> underGradTable = table key(name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 },
            { id: 3, name: "Jane", grade: 13 }
    ];

    anydata|error tbl = s1.fromBalString();
    assert(tbl is table<map<anydata>>, true);
    if (tbl is table<map<anydata>>) {
        tbl.add({ id: 3, name: "Jane", grade: 13 });
        assert(tbl, underGradTable);
    }

    anydata|error tbl2 = s2.fromBalString();
    assert(tbl2 is anydata, true);
    if (tbl2 is anydata) {
        assert(tbl2, table [{ id: 1, name: "Mary", grade: 12 }, { id: 2, name: "John", grade: 13 }]);
    }

    anydata|error tbl3 = s3.fromBalString();
    assert(tbl3 is anydata, true);
    if (tbl3 is anydata) {
        table<UndergradStudent> key(id, name) underGradTable2 = table [];
        assert(tbl3, underGradTable2);
    }
}

function testArrayFromBalString() {
    decimal x1 = 345.2425341;
    error err = error("Failed to get account balance", details = true, val1 = (0.0/0.0), val2 = "This Error",
           val3 = {"x":"AA","y":(1.0/0.0)});
    xml xmlVal = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`;
    table<UndergradStudent> underGradTable = table key(id,name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 }
    ];

    int[] arr1 = [1, 2, 3, 4, 5];
    float[] arr2 = [12, 12.34, (0.0/0.0), (1.0/0.0)];
    boolean[] arr3 = [true, false, true, true];
    byte[] arr4 = [12, 10, 9, 8];
    string[] arr5 = ["ABC", "XYZ", "LMN"];
    decimal[] arr6 = [12.65, 1, 2, 90.0];
    anydata[] arr7 = ["str", 23, 23.4, true, {"x":"AA","y":(1.0/0.0),"z":1.23}, x1, ["X", (0.0/0.0),
    x1], underGradTable, xmlVal];
    string[] arr8 = [];

    string s1 = "[1,2,3,4,5]";
    string s2 = "[12.0,12.34,float:NaN,float:Infinity]";
    string s3 = "[true,false,true,true]";
    string s4 = "[12,10,9,8]";
    string s5 = "[\"ABC\",\"XYZ\",\"LMN\"]";
    string s6 = "[12.65d,1d,2d,90.0d]";
    string s7 = "[\"str\",23,23.4,true,{\"x\":\"AA\",\"y\":float:Infinity,\"z\":1.23},345.2425341d," +
      "[\"X\",float:NaN,345.2425341d],xml`<CATALOG><CD>" +
      "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`]";
    string s8 = "[]";

    assert(s1.fromBalString(), arr1);
    assert(s2.fromBalString(), arr2);
    assert(s3.fromBalString(), arr3);
    assert(s4.fromBalString(), arr4);
    assert(s5.fromBalString(), arr5);
    assert(s6.fromBalString(), arr6);

    anydata|error result = s7.fromBalString();
    if (result is anydata[]) {
        assert(result[0], arr7[0]);
        assert(result[1], arr7[1]);
        assert(result[2], arr7[2]);
        assert(result[3], arr7[3]);
        assert(result[4], arr7[4]);
        assert(result[5], arr7[5]);
        assert(result[6], arr7[6]);
        assert(result[7], arr7[8]);
    }

    assert(s8.fromBalString(), arr8);
}

function testTupleFromBalString() {
    [string, int, decimal, float] tupleVal = ["TOM", 10, 90.12, (0.0/0.0)];
    [string, int, decimal, float] tupleVal2 = [];

    string s1 = "[\"TOM\",10,90.12d,float:NaN]";
    string s2 = "[\"\",0,0.0d,0.0]";

    anydata|error result = s1.fromBalString();

    if (result is [string, int, decimal, float]) {
        assert(result, tupleVal);
    }

    assert(s2.fromBalString(), tupleVal2);
}

function testJsonFromBalString() {
    json jsonVal = {a: "STRING", b: 12, c: 12.4, d: true, e: {x:"x", y: ()}};

    string s1 = "{\"a\":\"STRING\",\"b\":12,\"c\":12.4,\"d\":true,\"e\":{\"x\":\"x\",\"y\":()}}";

    anydata|error result = s1.fromBalString();

    if (result is json) {
        assert(result, jsonVal);
    }
}

function testXmlFromBalString() {
    xml x1 = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?xxtarget this mk data this?>`;
    xml x5 = x1 + x2 + x3 + x4;

    string s1 = x1.toBalString();
    string s2 = x2.toBalString();
    string s3 = x3.toBalString();
    string s4 = x4.toBalString();
    string s5 = x5.toBalString();

    anydata|error result1 = s1.fromBalString();
    anydata|error result2 = s2.fromBalString();
    anydata|error result3 = s3.fromBalString();
    anydata|error result4 = s4.fromBalString();
    anydata|error result5 = s5.fromBalString();

    if (result1 is xml && result2 is xml && result3 is xml && result4 is xml && result5 is xml) {
        assert(result1, x1);
        assert(result2, x2);
        assert(result3, x3);
        assert(result4, x4);
        assert(result5, x5);
    }

    xml xmlVal = xml `<movie>
                        <title>Some</title>
                        <writer>Writer</writer>
                      </movie>`;

    string xmlString = xmlVal.toBalString();
    string expectedXmlString = "xml`<movie>\n                        <title>Some</title>"
    + "\n                        <writer>Writer</writer>\n                      </movie>`";
    assert(xmlString, expectedXmlString);
    anydata xmlValBack = checkpanic xmlString.fromBalString();
    assert(xmlValBack, xmlVal);
}

function testObjectFromString() {
    Student obj1 = new("Alaa", "MMV");
    Teacher obj2 = new("Rola", "MMV");

    string s1 = values:toBalString(obj1);
    string s2 = values:toBalString(obj2);

    anydata|error result1 = s1.fromBalString();
    anydata|error result2 = s2.fromBalString();

    assert(result1 is error, true);
    assert(result2 is error, true);
}

function testFromBalStringOnCycles() {
     string s1 = "{\"ee\":3,\"1\":{\"mm\":5,\"1\":...[2],\"2\":{\"qq\":5,\"1\":[2,3,5,...[5]]," +
         "\"2\":...[4],\"3\":...[0],\"4\":...[2]}},\"2\":{\"qq\":5,\"1\":[2,3,5,...[3]],\"2\":...[2],\"3\":...[0]," +
         "\"4\":{\"mm\":5,\"1\":...[4],\"2\":...[2]}},\"3\":...[0]}";

     anydata result = checkpanic s1.fromBalString();
     assert(result.toBalString(), s1);
}

function testFromBalStringNegative() {
    string a = "Mike";
    string b = "009 abd []";
    string c = " 009%abd {gf& *";
    string d = "0.34t.\"";
    error err = error("{ballerina/lang.value}FromBalStringError",message="invalid expression style string value");
    assert(a.fromBalString(), err);
    assert(b.fromBalString(), err);
    assert(c.fromBalString(), err);
    assert(d.fromBalString(), err);
}

function testFromStringOnRegExp() {
    string s = "re `AB+C*D{1,4}`";
    anydata x1 = checkpanic s.fromBalString();
    assert(re `AB+C*D{1,4}` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `A?B+C*?D{1,4}`";
    x1 = checkpanic s.fromBalString();
    assert(re `A?B+C*?D{1,4}` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `A\\sB\\WC\\Dd\\\\`";
    x1 = checkpanic s.fromBalString();
    assert(re `A\sB\WC\Dd\\` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `\\s{1}\\p{sc=Braille}*`";
    x1 = checkpanic s.fromBalString();
    assert(re `\s{1}\p{sc=Braille}*` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `AB+\\p{gc=Lu}{1,}`";
    x1 = checkpanic s.fromBalString();
    assert(re `AB+\p{gc=Lu}{1,}` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `A\\p{Lu}??B+\\W\\(+?C*D{1,4}?`";
    x1 = checkpanic s.fromBalString();
    assert(re `A\p{Lu}??B+\W\(+?C*D{1,4}?` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*`";
    x1 = checkpanic s.fromBalString();
    assert(re `\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[\\r\\n\\^]`";
    x1 = checkpanic s.fromBalString();
    assert(re `[\r\n\^]` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[A\\sB\\WC\\Dd\\\\]`";
    x1 = checkpanic s.fromBalString();
    assert(re `[A\sB\WC\Dd\\]` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??`";
    x1 = checkpanic s.fromBalString();
    assert(re `[\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[A\\sA-GB\\WC\\DJ-Kd\\\\]*`";
    x1 = checkpanic s.fromBalString();
    assert(re `[A\sA-GB\WC\DJ-Kd\\]*` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[\\sA-F\\p{sc=Braille}K-Mabc-d\\--]`";
    x1 = checkpanic s.fromBalString();
    assert(re `[\sA-F\p{sc=Braille}K-Mabc-d\--]` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?`";
    x1 = checkpanic s.fromBalString();
    assert(re `[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?:ABC)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?:ABC)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?i:ABC)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?i:ABC)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?i-m:AB+C*)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?i-m:AB+C*)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?imxs:AB+C*D{1,4})`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?imxs:AB+C*D{1,4})` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?imx-s:A?B+C*?D{1,4})`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?imx-s:A?B+C*?D{1,4})` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?i-s:\\s{1}\\p{sc=Braille}*)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?i-s:\s{1}\p{sc=Braille}*)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?ims-x:\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?ims-x:\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?im-sx:[A\\sA-GB\\WC\\DJ-Kd\\\\]*)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?im-sx:[A\sA-GB\WC\DJ-Kd\\]*)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?i-sxm:[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?)`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?i-sxm:[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?)` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef`";
    x1 = checkpanic s.fromBalString();
    assert(re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `(z)((a+)?(b+)?(c))*`";
    x1 = checkpanic s.fromBalString();
    assert(re `(z)((a+)?(b+)?(c))*` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `^^^^^^^robot$$$$`";
    x1 = checkpanic s.fromBalString();
    assert(re `^^^^^^^robot$$$$` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `cx{0,93}c`";
    x1 = checkpanic s.fromBalString();
    assert(re `cx{0,93}c` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `[\\d]*[\\s]*bc.`";
    x1 = checkpanic s.fromBalString();
    assert(re `[\d]*[\s]*bc.` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `\\??\\??\\??\\??\\??`";
    x1 = checkpanic s.fromBalString();
    assert(re `\??\??\??\??\??` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `.?.?.?.?.?.?.?`";
    x1 = checkpanic s.fromBalString();
    assert(re `.?.?.?.?.?.?.?` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `bc..[\\d]*[\\s]*`";
    x1 = checkpanic s.fromBalString();
    assert(re `bc..[\d]*[\s]*` == x1, true);
    assert(x1 is string:RegExp, true);

    s = "re `\\\\u123`";
    x1 = checkpanic s.fromBalString();
    assert(re `\\u123` == x1, true);
    assert(x1 is string:RegExp, true);
}

function testFromStringOnRegExpNegative() {
    string s = "re `AB+^*`";
    anydata|error x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing backslash before '*' token in 'AB+^*'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `AB\\hCD`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: invalid character 'h' after backslash in 'AB\\hCD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `AB\\pCD`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing open brace '{' token in 'AB\\pCD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `AB\\uCD`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: invalid character 'u' after backslash in 'AB\\uCD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `AB\\u{001CD`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing close brace '}' token in 'AB\\u{001CD'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `AB\\p{sc=Lu`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing close brace '}' token in 'AB\\p{sc=Lu'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `[^abc`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing close bracket ']' token in '[^abc'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `(abc`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing close parenthesis ')' token in '(abc'",
        <string>checkpanic (<error>x1).detail()["message"]);

    s = "re `(ab^*)`";
    x1 = s.fromBalString();
    assert(x1 is error, true);
    assert("{ballerina/lang.value}FromBalStringError", (<error>x1).message());
    assert("Failed to parse regular expression: missing backslash before '*' token in '(ab^*)'",
        <string>checkpanic (<error>x1).detail()["message"]);
}

function assert(anydata|error actual, anydata|error expected) {
    if (!isEqual(actual, expected)) {
        string expectedValAsString = expected is error ? expected.toString() : expected.toString();
        string actualValAsString = actual is error ? actual.toString() : actual.toString();

        typedesc<anydata|error> expT = typeof expected;
        typedesc<anydata|error> actT = typeof actual;
        string reason = "expected [" + expectedValAsString + "] of type [" + expT.toString()
                            + "], but found [" + actualValAsString + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

isolated function isEqual(values:Cloneable actual, values:Cloneable expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else if (actual is error && expected is error) {
        return actual.message() == expected.message() &&
            isEqual(actual.cause(), expected.cause()) &&
            isEqual(actual.detail(), expected.detail());
    } else {
        return (actual === expected);
    }
}
