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

import ballerina/lang.'string as strings;
import ballerina/lang.'int as ints;
import ballerina/lang.value;

type Address record {
    string country;
    string city;
    string street;
};

type Person record {
    string name;
    Address address;
    int age;

};

type Employee record {
    readonly int id;
    int age;
    decimal salary;
    string name;
    boolean married;
};

type UndergradStudent record {|
    readonly int id;
    readonly string name;
    int grade;
|};

public type AnotherDetail record {
    string message;
    error cause?;
};

public const REASON_1 = "Reason1";

public type FirstError distinct error<AnotherDetail>;

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

distinct class Foo {
    int i = 0;
}

distinct class Bar {
    string name;

    function init(string name) {
        self.name = name;
    }

    function toString() returns string {
        return "Bar name is " + self.name;
    }
}

function testIntValueToBalString() {
    int x1 = 12;
    ints:Signed32 x2 = 2147483647;
    ints:Signed16 x3 = -32768;
    ints:Signed8 x4 = 100;
    ints:Unsigned32 x5 = 4294967295;
    ints:Unsigned16 x6 = 450;
    ints:Unsigned8 x7 = 221;
    byte x8 = 10;

    assert(x1.toBalString(), "12");
    assert(x2.toBalString(), "2147483647");
    assert(x3.toBalString(), "-32768");
    assert(x4.toBalString(), "100");
    assert(x5.toBalString(), "4294967295");
    assert(x6.toBalString(), "450");
    assert(x7.toBalString(), "221");
    assert(x8.toBalString(), "10");
}

function testStringValueToBalString() {
    string a = "Anne";
    strings:Char b = "m";

    assert(a.toBalString(), "\"Anne\"");
    assert(b.toBalString(), "\"m\"");
}

function testFloatingPointNumbersToBalString() {
    float x1 = 12.342;
    float x2 = 0.0/0.0;
    float x3 = 4.0/0.0;
    decimal x4 = 345.2425341;
    decimal x5 = 1;

    assert(x1.toBalString(), "12.342");
    assert(x2.toBalString(), "float:NaN");
    assert(x3.toBalString(), "float:Infinity");
    assert(x4.toBalString(), "345.2425341d");
    assert(x5.toBalString(), "1d");
}

function testAnyAnydataNilToBalString() {
    anydata a = 99;
    any b = a;
    () c = ();

    assert(a.toBalString(), "99");
    assert(b.toBalString(), "99");
    assert(c.toBalString(), "()");
}

function testTableToBalString() {
    table<UndergradStudent> underGradTable1 = table key(id,name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 }
    ];
    table<UndergradStudent> underGradTable2 = table [
                { id: 1, name: "Mary", grade: 12 },
                { id: 2, name: "John", grade: 13 }
    ];

    assert(underGradTable1.toBalString(), "table key(id,name) [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
                                          "{\"id\":2,\"name\":\"John\",\"grade\":13}]");
    assert(underGradTable2.toBalString(), "table key() [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
                                              "{\"id\":2,\"name\":\"John\",\"grade\":13}]");
}

function testErrorToBalString() {
    error err1 = error("Failed to get account balance", details = true, val1 = (0.0/0.0), val2 = "This Error",
           val3 = {"x":"AA","y":(1.0/0.0)});
    FirstError err2 = error FirstError(REASON_1, message = "Test passing error union to a function");
    error err3 = error("first error", detail=(1.0/0.0));
    error err4 = error("second error", err3);

    assert(err1.toBalString(), "error(\"Failed to get account balance\",details=true,val1=float:NaN," +
    "val2=\"This Error\",val3={\"x\":\"AA\",\"y\":float:Infinity})");
    assert(err2.toBalString(), "error FirstError (\"Reason1\",message=\"Test passing error union to a function\")");
    assert(err4.toBalString(), "error(\"second error\",error(\"first error\",detail=float:Infinity))");
}

function testMapToBalString() {
    map<string> mapVal1 = {"name":"ABC", "school":"City College"};
    map<any|error> mapVal2 = {"1":12, "2":"James", "3":{"x":"AA","y":(1.0/0.0)}, "4":(),
            "5":error("Failed to get account balance", details = true)};

    assert(mapVal1.toBalString(), "{\"name\":\"ABC\",\"school\":\"City College\"}");
    assert(mapVal2.toBalString(), "{\"1\":12,\"2\":\"James\",\"3\":{\"x\":\"AA\",\"y\":float:Infinity},\"4\":()," +
         "\"5\":error error (\"Failed to get account balance\",details=true)}");
}

function testArrayToBalString() {
    decimal x1 = 345.2425341;
    table<UndergradStudent> underGradTable = table key(id,name) [
            { id: 1, name: "Mary", grade: 12 },
            { id: 2, name: "John", grade: 13 }
    ];
    error err = error("Failed to get account balance", details = true, val1 = (0.0/0.0), val2 = "This Error",
           val3 = {"x":"AA","y":(1.0/0.0)});
    xml xmlVal = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`;

    int[] arr1 = [1, 2, 3, 4, 5];
    float[] arr2 = [12, 12.34, (0.0/0.0), (1.0/0.0)];
    boolean[] arr3 = [true, false, true, true];
    byte[] arr4 = [12, 10, 9, 8, 100, 125, 137, 128, 150, 200, 255];
    string[] arr5 = ["ABC", "XYZ", "LMN"];
    decimal[] arr6 = [12.65, 1, 2, 90.0];
    (any|error)[] arr7 = ["str", 23, 23.4, true, {"x":"AA","y":(1.0/0.0),"z":1.23}, x1, ["X", (0.0/0.0),
    x1], underGradTable, err, xmlVal];

    assert(arr1.toBalString(), "[1,2,3,4,5]");
    assert(arr2.toBalString(), "[12.0,12.34,float:NaN,float:Infinity]");
    assert(arr2.toBalString(), "[12.0,12.34,float:NaN,float:Infinity]");
    assert(arr3.toBalString(), "[true,false,true,true]");
    assert(arr4.toBalString(), "[12,10,9,8,100,125,137,128,150,200,255]");
    assert(arr5.toBalString(), "[\"ABC\",\"XYZ\",\"LMN\"]");
    assert(arr6.toBalString(), "[12.65d,1d,2d,90.0d]");
    assert(arr7.toBalString(), "[\"str\",23,23.4,true,{\"x\":\"AA\",\"y\":float:Infinity,\"z\":1.23},345.2425341d," +
    "[\"X\",float:NaN,345.2425341d],table key(id,name) [{\"id\":1,\"name\":\"Mary\",\"grade\":12}," +
    "{\"id\":2,\"name\":\"John\",\"grade\":13}],error(\"Failed to get account balance\",details=true," +
    "val1=float:NaN,val2=\"This Error\",val3={\"x\":\"AA\",\"y\":float:Infinity}),xml`<CATALOG><CD>" +
    "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`]");
}

function testTupleToBalString() {
    [string, int, decimal, float] tupleVal = ["TOM", 10, 90.12, (0.0/0.0)];

    assert(tupleVal.toBalString(), "[\"TOM\",10,90.12d,float:NaN]");
}

function testJsonToBalString() {
    json jsonVal = {a: "STRING", b: 12, c: 12.4, d: true, e: {x:"x", y: ()}};

    assert(jsonVal.toBalString(), "{\"a\":\"STRING\",\"b\":12,\"c\":12.4,\"d\":true," +
                                    "\"e\":{\"x\":\"x\",\"y\":()}}");

}

function testXmlToBalString() {
    xml xmlVal = xml `<CATALOG><CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`;

    assert(xmlVal.toBalString(), "xml`<CATALOG><CD><TITLE>Empire Burlesque</TITLE>" +
         "<ARTIST>Bob Dylan</ARTIST></CD></CATALOG>`");
    assert((xmlVal/*).toBalString(), "xml`<CD><TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST></CD>`");

}

function testObjectToBalString() {
    Student obj1 = new("Alaa", "MMV");
    Student obj2 = new("Alaa", "MMV");
    Student obj3 = obj1;
    Teacher obj4 = new("Rola", "MMV");
    Foo obj5 = new;
    Bar obj6 = new("Old Haunt");

    assert(value:toBalString(obj1) === value:toBalString(obj2), false);
    assert(value:toBalString(obj1) === value:toBalString(obj3), true);
    assert(strings:startsWith(value:toBalString(obj3), "object "), true);
    assert(value:toBalString(obj4), "object Rola from MMV");
    assert(strings:startsWith(value:toBalString(obj5), "object Foo"), true);
    assert(value:toBalString(obj6), "object Bar name is Old Haunt");
}

function testToBalStringOnCycles() {
     map<anydata> x = {"ee" : 3};
     map<anydata> y = {"qq" : 5};
     map<anydata> z = {"mm" : 5};
     anydata[] arr = [2 , 3, 5];
     x["1"] = z;
     x["2"] = y;
     x["3"] = x;
     y["1"] = arr;
     y["2"] = x;
     y["3"] = y;
     y["4"] = z;
     z["1"] = x;
     z["2"] = y;
     arr.push(x);

     assert(x.toBalString(), "{\"ee\":3,\"1\":{\"mm\":5,\"1\":...[2],\"2\":{\"qq\":5,\"1\":[2,3,5,...[5]]," +
     "\"2\":...[4],\"3\":...[0],\"4\":...[2]}},\"2\":{\"qq\":5,\"1\":[2,3,5,...[3]],\"2\":...[2],\"3\":...[0]," +
     "\"4\":{\"mm\":5,\"1\":...[4],\"2\":...[2]}},\"3\":...[0]}");
}

function testToBalStringOnRegExpValueWithLiterals() {
    string:RegExp x1 = re `A`;
    assert("re `A`", x1.toBalString());

    string:RegExp x2 = re `ABC`;
    assert("re `ABC`", x2.toBalString());

    string:RegExp x3 = re `AB+C*`;
    assert("re `AB+C*`", x3.toBalString());

    string:RegExp x4 = re `AB+C*D{1}`;
    assert("re `AB+C*D{1}`", x4.toBalString());

    string:RegExp x5 = re `AB+C*D{1,}`;
    assert("re `AB+C*D{1,}`", x5.toBalString());

    string:RegExp x6 = re `AB+C*D{1,4}`;
    assert("re `AB+C*D{1,4}`", x6.toBalString());

    string:RegExp x7 = re `A?B+C*?D{1,4}`;
    assert("re `A?B+C*?D{1,4}`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithEscapes() {
    string:RegExp x1 = re `\r\n\^`;
    assert("re `\\r\\n\\^`", x1.toBalString());

    string:RegExp x2 = re `\*\d`;
    assert("re `\\*\\d`", x2.toBalString());

    string:RegExp x3 = re `A\sB\WC\Dd\\`;
    assert("re `A\\sB\\WC\\Dd\\\\`", x3.toBalString());

    string:RegExp x4 = re `\s{1}\p{sc=Braille}*`;
    assert("re `\\s{1}\\p{sc=Braille}*`", x4.toBalString());

    string:RegExp x5 = re `AB+\p{gc=Lu}{1,}`;
    assert("re `AB+\\p{gc=Lu}{1,}`", x5.toBalString());

    string:RegExp x6 = re `A\p{Lu}??B+\W\(+?C*D{1,4}?`;
    assert("re `A\\p{Lu}??B+\\W\\(+?C*D{1,4}?`", x6.toBalString());

    string:RegExp x7 = re `\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*`;
    assert("re `\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCharacterClass() {
    string:RegExp x1 = re `[\r\n\^]`;
    assert("re `[\\r\\n\\^]`", x1.toBalString());

    string:RegExp x2 = re `[\*\d]`;
    assert("re `[\\*\\d]`", x2.toBalString());

    string:RegExp x3 = re `[A\sB\WC\Dd\\]`;
    assert("re `[A\\sB\\WC\\Dd\\\\]`", x3.toBalString());

    string:RegExp x4 = re `[\s\p{sc=Braille}]`;
    assert("re `[\\s\\p{sc=Braille}]`", x4.toBalString());

    string:RegExp x5 = re `[AB\p{gc=Lu}]+?`;
    assert("re `[AB\\p{gc=Lu}]+?`", x5.toBalString());

    string:RegExp x6 = re `[A\p{Lu}B\W\(CD]*`;
    assert("re `[A\\p{Lu}B\\W\\(CD]*`", x6.toBalString());

    string:RegExp x7 = re `[\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??`;
    assert("re `[\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCharacterClass2() {
    string:RegExp x1 = re `[^\r\n\^a-z]`;
    assert("re `[^\\r\\n\\^a-z]`", x1.toBalString());

    string:RegExp x2 = re `[\*a-d\de-f]`;
    assert("re `[\\*a-d\\de-f]`", x2.toBalString());

    string:RegExp x3 = re `[A\sA-GB\WC\DJ-Kd\\]*`;
    assert("re `[A\\sA-GB\\WC\\DJ-Kd\\\\]*`", x3.toBalString());

    string:RegExp x4 = re `[\sA-F\p{sc=Braille}K-Mabc-d\--]`;
    assert("re `[\\sA-F\\p{sc=Braille}K-Mabc-d\\--]`", x4.toBalString());

    string:RegExp x5 = re `[A-B\p{gc=Lu}\s-\SAD\s-\w]`;
    assert("re `[A-B\\p{gc=Lu}\\s-\\SAD\\s-\\w]`", x5.toBalString());

    string:RegExp x6 = re `[\s-\wA\p{Lu}B\W\(CDA-B]+?`;
    assert("re `[\\s-\\wA\\p{Lu}B\\W\\(CDA-B]+?`", x6.toBalString());

    string:RegExp x7 = re `[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?`;
    assert("re `[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCapturingGroups() {
    string:RegExp x1 = re `(?:ABC)`;
    assert("re `(?:ABC)`", x1.toBalString());

    string:RegExp x2 = re `(?i:ABC)`;
    assert("re `(?i:ABC)`", x2.toBalString());

    string:RegExp x3 = re `(?i-m:AB+C*)`;
    assert("re `(?i-m:AB+C*)`", x3.toBalString());

    string:RegExp x4 = re `(?im-sx:AB+C*D{1})`;
    assert("re `(?im-sx:AB+C*D{1})`", x4.toBalString());

    string:RegExp x5 = re `(?:AB+C*D{1,})`;
    assert("re `(?:AB+C*D{1,})`", x5.toBalString());

    string:RegExp x6 = re `(?imxs:AB+C*D{1,4})`;
    assert("re `(?imxs:AB+C*D{1,4})`", x6.toBalString());

    string:RegExp x7 = re `(?imx-s:A?B+C*?D{1,4})`;
    assert("re `(?imx-s:A?B+C*?D{1,4})`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCapturingGroups2() {
    string:RegExp x1 = re `(\r\n\^)`;
    assert("re `(\\r\\n\\^)`", x1.toBalString());

    string:RegExp x2 = re `(?:\*\d)`;
    assert("re `(?:\\*\\d)`", x2.toBalString());

    string:RegExp x3 = re `(?i:A\sB\WC\Dd\\)`;
    assert("re `(?i:A\\sB\\WC\\Dd\\\\)`", x3.toBalString());

    string:RegExp x4 = re `(?i-s:\s{1}\p{sc=Braille}*)`;
    assert("re `(?i-s:\\s{1}\\p{sc=Braille}*)`", x4.toBalString());

    string:RegExp x5 = re `(?ismx:AB+\p{gc=Lu}{1,})`;
    assert("re `(?ismx:AB+\\p{gc=Lu}{1,})`", x5.toBalString());

    string:RegExp x6 = re `(?im-sx:A\p{Lu}??B+\W\(+?C*D{1,4}?)`;
    assert("re `(?im-sx:A\\p{Lu}??B+\\W\\(+?C*D{1,4}?)`", x6.toBalString());

    string:RegExp x7 = re `(?ims-x:\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*)`;
    assert("re `(?ims-x:\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*)`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCapturingGroups3() {
    string:RegExp x1 = re `([\r\n\^])`;
    assert("re `([\\r\\n\\^])`", x1.toBalString());

    string:RegExp x2 = re `(?i:[\*\d])`;
    assert("re `(?i:[\\*\\d])`", x2.toBalString());

    string:RegExp x3 = re `(?i-s:[A\sB\WC\Dd\\])`;
    assert("re `(?i-s:[A\\sB\\WC\\Dd\\\\])`", x3.toBalString());

    string:RegExp x4 = re `(?ix:sm:[\s\p{sc=Braille}])`;
    assert("re `(?ix:sm:[\\s\\p{sc=Braille}])`", x4.toBalString());

    string:RegExp x5 = re `(?isxm:[AB\p{gc=Lu}]+?)`;
    assert("re `(?isxm:[AB\\p{gc=Lu}]+?)`", x5.toBalString());

    string:RegExp x6 = re `(?isx-m:[A\p{Lu}B\W\(CD]*)`;
    assert("re `(?isx-m:[A\\p{Lu}B\\W\\(CD]*)`", x6.toBalString());

    string:RegExp x7 = re `([\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??)`;
    assert("re `([\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??)`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCapturingGroups4() {
    string:RegExp x1 = re `([^\r\n\^a-z])`;
    assert("re `([^\\r\\n\\^a-z])`", x1.toBalString());

    string:RegExp x2 = re `(?i:[\*a-d\de-f])`;
    assert("re `(?i:[\\*a-d\\de-f])`", x2.toBalString());

    string:RegExp x3 = re `(?im-sx:[A\sA-GB\WC\DJ-Kd\\]*)`;
    assert("re `(?im-sx:[A\\sA-GB\\WC\\DJ-Kd\\\\]*)`", x3.toBalString());

    string:RegExp x4 = re `(?i-s:[\sA-F\p{sc=Braille}K-Mabc-d\--])`;
    assert("re `(?i-s:[\\sA-F\\p{sc=Braille}K-Mabc-d\\--])`", x4.toBalString());

    string:RegExp x5 = re `(?imx-s:[A-B\p{gc=Lu}\s-\SAD\s-\w])`;
    assert("re `(?imx-s:[A-B\\p{gc=Lu}\\s-\\SAD\\s-\\w])`", x5.toBalString());

    string:RegExp x6 = re `(?imxs:[\s-\wA\p{Lu}B\W\(CDA-B]+?)`;
    assert("re `(?imxs:[\\s-\\wA\\p{Lu}B\\W\\(CDA-B]+?)`", x6.toBalString());

    string:RegExp x7 = re `(?i-sxm:[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?)`;
    assert("re `(?i-sxm:[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?)`", x7.toBalString());
}

function testToBalStringOnRegExpValueWithCapturingGroups5() {
    string:RegExp x1 = re `((ab)|(a))((c)|(bc))`;
    assert("re `((ab)|(a))((c)|(bc))`", x1.toBalString());

    string:RegExp x2 = re `(?:ab|cd)+|ef`;
    assert("re `(?:ab|cd)+|ef`", x2.toBalString());

    string:RegExp x3 = re `(?:(?i-m:ab|cd))+|ef`;
    assert("re `(?:(?i-m:ab|cd))+|ef`", x3.toBalString());

    string:RegExp x4 = re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef`;
    assert("re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef`", x4.toBalString());

    string:RegExp x5 = re `(z)((a+)?(b+)?(c))*`;
    assert("re `(z)((a+)?(b+)?(c))*`", x5.toBalString());

    string:RegExp x6 = re `(?:)`;
    assert("re `(?:)`", x6.toBalString());

    string:RegExp x7 = re `(?i:a|b)`;
    assert("re `(?i:a|b)`", x7.toBalString());

    string:RegExp x8 = re `(?im:a|b|[])`;
    assert("re `(?im:a|b|[])`", x8.toBalString());

    string:RegExp x9 = re `(?i-m:[0-9])`;
    assert("re `(?i-m:[0-9])`", x9.toBalString());

    string:RegExp x10 = re `(?im-s:[z-z])`;
    assert("re `(?im-s:[z-z])`", x10.toBalString());

    string:RegExp x11 = re `(?im-sx:abc{1})`;
    assert("re `(?im-sx:abc{1})`", x11.toBalString());

    string:RegExp x12 = re `(?im:\\u0042)`;
    assert("re `(?im:\\\\u0042)`", x12.toBalString());

    string:RegExp x13 = re `java(script)?`;
    assert("re `java(script)?`", x13.toBalString());

    string:RegExp x14 = re `(x*)(x+)`;
    assert("re `(x*)(x+)`", x14.toBalString());

    string:RegExp x15= re `(\d*)(\d+)`;
    assert("re `(\\d*)(\\d+)`", x15.toBalString());

    string:RegExp x16= re `(\d*)\d(\d+)`;
    assert("re `(\\d*)\\d(\\d+)`", x16.toBalString());
}

function testToBalStringOnComplexRegExpValue() {
    string:RegExp x1 = re `s$`;
    assert("re `s$`", x1.toBalString());

    string:RegExp x2 = re `^[^p]`;
    assert("re `^[^p]`", x2.toBalString());

    string:RegExp x3 = re `^p[b-z]`;
    assert("re `^p[b-z]`", x3.toBalString());

    string:RegExp x4 = re `^ab`;
    assert("re `^ab`", x4.toBalString());

    string:RegExp x5 = re `^\^+`;
    assert("re `^\\^+`", x5.toBalString());

    string:RegExp x6 = re `^^^^^^^robot$$$$`;
    assert("re `^^^^^^^robot$$$$`", x6.toBalString());

    string:RegExp x7 = re `^.*?$`;
    assert("re `^.*?$`", x7.toBalString());

    string:RegExp x8 = re `^.*?(:|$)`;
    assert("re `^.*?(:|$)`", x8.toBalString());

    string:RegExp x9 = re `^.*(:|$)`;
    assert("re `^.*(:|$)`", x9.toBalString());

    string:RegExp x10 = re `\d{2,4}`;
    assert("re `\\d{2,4}`", x10.toBalString());

    string:RegExp x11 = re `cx{0,93}c`;
    assert("re `cx{0,93}c`", x11.toBalString());

    string:RegExp x12 = re `B{42,93}c`;
    assert("re `B{42,93}c`", x12.toBalString());

    string:RegExp x13 = re `[^"]*`;
    assert("re `[^\"]*`", x13.toBalString());

    string:RegExp x14 = re `["'][^"']*["']`;
    assert("re `[\"'][^\"']*[\"']`", x14.toBalString());

    string:RegExp x15 = re `cd*`;
    assert("re `cd*`", x15.toBalString());

    string:RegExp x16 = re `x*y+$`;
    assert("re `x*y+$`", x16.toBalString());

    string:RegExp x17 = re `[\d]*[\s]*bc.`;
    assert("re `[\\d]*[\\s]*bc.`", x17.toBalString());

    string:RegExp x18 = re `.*`;
    assert("re `.*`", x18.toBalString());

    string:RegExp x19 = re `[xyz]*1`;
    assert("re `[xyz]*1`", x19.toBalString());

    string:RegExp x20 = re `cd?e`;
    assert("re `cd?e`", x20.toBalString());

    string:RegExp x21 = re `cdx?e`;
    assert("re `cdx?e`", x21.toBalString());

    string:RegExp x22 = re `X?y?z?`;
    assert("re `X?y?z?`", x22.toBalString());

    string:RegExp x23 = re `ab?c?d?x?y?z`;
    assert("re `ab?c?d?x?y?z`", x23.toBalString());

    string:RegExp x24 = re `\??\??\??\??\??`;
    assert("re `\\??\\??\\??\\??\\??`", x24.toBalString());

    string:RegExp x25 = re `.?.?.?.?.?.?.?`;
    assert("re `.?.?.?.?.?.?.?`", x25.toBalString());

    string:RegExp x26 = re `\S+`;
    assert("re `\\S+`", x26.toBalString());

    string:RegExp x27 = re `bc..[\d]*[\s]*`;
    assert("re `bc..[\\d]*[\\s]*`", x27.toBalString());

    string:RegExp x28 = re `\?`;
    assert("re `\\?`", x28.toBalString());

    string:RegExp x29 = re `\\`;
    assert("re `\\\\`", x29.toBalString());

    string:RegExp x30 = re `\*`;
    assert("re `\\*`", x30.toBalString());

    string:RegExp x31 = re `\\u123`;
    assert("re `\\\\u123`", x31.toBalString());

    string:RegExp x32 = re `\\3`;
    assert("re `\\\\3`", x32.toBalString());
}

function testToBalStringComplexRegExpValue2() {
    int val = 10;
    string:RegExp x1 = re `^ab${val}cd*e$`;
    assert("re `^ab10cd*e$`", x1.toBalString());

    string:RegExp x2 = re `$[^a-b\tx-z]+(?i:ab^c[d-f]){12,}`;
    assert("re `$[^a-b\\tx-z]+(?i:ab^c[d-f]){12,}`", x2.toBalString());

    string:RegExp x3 = re `(x|y)|z|[cde-j]*|(?ms:[^])`;
    assert("re `(x|y)|z|[cde-j]*|(?ms:[^])`", x3.toBalString());

    string:RegExp x4 = re `[^ab-cdk-l]*${val}+|(?:pqr{1,}[a=cdegf\s-\Seg-ijk-])`;
    assert("re `[^ab-cdk-l]*10+|(?:pqr{1,}[a=cdegf\\s-\\Seg-ijk-])`", x4.toBalString());

    string:RegExp x5 = re `a\td-*ab[^c-f]+(?m:xj(?i:x|y))`;
    assert("re `a\\td-*ab[^c-f]+(?m:xj(?i:x|y))`", x5.toBalString());
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
