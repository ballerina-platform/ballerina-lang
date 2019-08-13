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

type Person record {
    string name;
    int age;
};

function testRecToJson() returns json|error {
    Person  p = {name: "N", age: 3};
    json|error j = typedesc<json>.constructFrom(p);
    return j;
}

function testOnTypeName() returns [Person|error, json|error] {
   json p = { name : "tom", age: 2};
   Person|error pp = Person.constructFrom(p);

   Person s = { name : "bob", age: 4};
   json|error ss = json.constructFrom(s);

   return [pp, ss];
}

type BRec record {
    int i = 0;
};

type CRec record {
    int i?;
};

public function testOptionalFieldToMandotoryField() returns any|error {
    CRec c = {  };
    var b = BRec.constructFrom(c);
    return b;
}

type Foo record {
    string s;
};

type Bar record {|
    float f?;
    string s;
|};

type Baz record {|
    int i?;
    string s;
|};

function testAmbiguousTargetType() returns boolean {
    Foo f = { s: "test string" };
    Bar|Baz|error bbe = (Bar|Baz).constructFrom(f);
    return bbe is error && bbe.reason() == "{ballerina}ConversionError" &&
            bbe.detail()?.message == "'Foo' value cannot be converted to 'Bar|Baz': ambiguous target type";
}

function testConstructFromForNilPositive() returns boolean {
    anydata a = ();
    string|error? c1 = (string?).constructFrom(a);
    json|error c2 = json.constructFrom(a);
    return c1 is () && c2 is ();
}

function testConstructFromForNilNegative() returns boolean {
    anydata a = ();
    string|int|error c1 = (string|int).constructFrom(a);
    Foo|Bar|error c2 = (Foo|Bar).constructFrom(a);
    return c1 is error && c2 is error && c1.detail()?.message == "cannot convert '()' to type 'string|int'";
}
