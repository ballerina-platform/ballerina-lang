// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.value;

/////////////////////////// Tests for `fromJsonWithType()` ///////////////////////////
type StringArray string[];

type Student2 record {
    string name;
    int age;
};

function testFromJsonWIthTypeNegative() {
    string s = "foobar";
    int|error zz = s.fromJsonWithType(int);
    assertEquality(zz is error, true);
}

function testFromJsonWithTypeRecord1() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    json j = <json> checkpanic str.fromJsonString();
    Student2|error p = j.fromJsonWithType(Student2);

    assertEquality(p is Student2, true);
    assertEquality((checkpanic p).toString(), "{\"name\":\"Name\",\"age\":35}");
}

type Student3 record {
    string name;
    int age?;
};

type Foo2 record {|
    int x2 = 1;
    int y3 = 2;
|};

type Foo3 record {
    string x3;
    int y3;
};

type Foo4 record {
    string x3;
    int y3 = 1;
};

type Foo5 record {
    string x3;
    int y3?;
};

type Foo6 record {
    string x3;
};

function testFromJsonWithTypeRecord2() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    json j = <json> checkpanic str.fromJsonString();
    Student3|error p = j.fromJsonWithType(Student3);

    assertEquality(p is Student3, true);
    assertEquality((checkpanic p).toString(), "{\"name\":\"Name\",\"age\":35}");
}

function testFromJsonWithTypeRecord3() {
    json j = {x3: "Chat"};

    Foo2|error f2 = j.fromJsonWithType(Foo2);
    assertEquality(f2 is error, true);

    Foo3|error f3 = j.fromJsonWithType(Foo3);
    assertEquality(f2 is error, true);

    Foo4|error f4 = j.fromJsonWithType(Foo4);
    assertEquality(f4 is Foo4, true);

    Foo5|error f5 = j.fromJsonWithType(Foo5);
    assertEquality(f5 is Foo5, true);
}

type Student2Or3 Student2|Student3;

type PetByAge record {
    int age;
    string nickname?;
};

type PetByType record {
    "Cat"|"Dog" pet_type;
    boolean hunts?;
};

type Pet PetByAge|PetByType;

function testFromJsonWithTypeAmbiguousTargetType() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    json j = <json> checkpanic str.fromJsonString();
    Student3|error p = j.fromJsonWithType(Student2Or3);
    assertEquality(p is error, false);
    assertEquality(p is Student3, true);
    assertEquality(p is Student2Or3, true);
    assertEquality(checkpanic p, <Student3> {name: "Name", age: 35});

    json jval = {
        "nickname": "Fido",
        "pet_type": "Dog",
        "age": 4
    };

    Pet pet = checkpanic jval.fromJsonWithType();
    assertEquality(pet is PetByAge, true);
    assertEquality(pet is PetByType, false);
    assertEquality(checkpanic pet, <PetByAge>{"nickname": "Fido", "pet_type": "Dog", age: 4});
}

type XmlType xml;

function testFromJsonWithTypeXML() {
    string s1 = "<test>name</test>";
    xml|error xe = s1.fromJsonWithType(XmlType);
    assertEquality(xe is xml, true);

    xml x = checkpanic xe;
    json j = x.toJson();
    assertEquality(j, s1);
}

type Student4 record {
    int id;
    xml x;
};

function testFromJsonWithTypeRecordWithXMLField() {
    Student4 student = {id: 1, x: xml `<book>DJ</book>`};
    json j = <json> student.toJson();
    Student4|error ss = j.fromJsonWithType(Student4);
    assertEquality(ss is Student4, true);
}

type MapOfAnyData map<anydata>;

function testFromJsonWithTypeMap() {
    json movie = {
        title: "Some",
        year: 2010
    };
    map<anydata> movieMap = checkpanic movie.fromJsonWithType(MapOfAnyData);
    assertEquality(movieMap["title"], "Some");
    assertEquality(movieMap["year"], 2010);
}

function testFromJsonWithTypeStringArray() {
    json j = ["Hello", "World"];
    string[] a = checkpanic j.fromJsonWithType(StringArray);
    string[] a2 = <string[]> a;
    assertEquality(a2.length(), 2);
    assertEquality(a2[0], "Hello");
}

function testFromJsonWithTypeArrayNegative() {
    json j = [1, 2];
    string[]|error s = j.fromJsonWithType(StringArray);
    assertEquality(s is error, true);
}

type IntArray int[];

function testFromJsonWithTypeIntArray() {
    json j = [1, 2];
    int[] arr = checkpanic j.fromJsonWithType(IntArray);
    int[] intArr = <int[]> arr;
    assertEquality(intArr[0], 1);
    assertEquality(intArr[1], 2);
}

type TableFoo2 table<Foo2>;
type TableFoo3 table<Foo3>;
type TableFoo4 table<Foo4>;
type TableFoo5 table<Foo5>;
type TableFoo6 table<Foo6>;

function testFromJsonWithTypeTable() {
    json jj = [
        {x3: "abc"},
        {x3: "abc"}
    ];

    table<Foo2>|error tabJ2 = jj.fromJsonWithType(TableFoo2);
    assertEquality(tabJ2 is error, true);

    table<Foo3>|error tabJ3 = jj.fromJsonWithType(TableFoo3);
    assertEquality(tabJ3 is error, true);

    table<Foo4>|error tabJ4 = jj.fromJsonWithType(TableFoo4);
    assertEquality(tabJ4 is table<Foo4>, true);

    table<Foo5>|error tabJ5 = jj.fromJsonWithType(TableFoo5);
    assertEquality(tabJ5 is table<Foo5>, true);

    table<Foo6>|error tabJ6 = jj.fromJsonWithType(TableFoo6);
    assertEquality(tabJ6 is table<Foo6>, true);

}

type IntVal record {int? x;};

type PostGradStudent record {|
    boolean employed;
    string first_name;
    string last_name?;
    PermanentAddress? address;
|};

type PermanentAddress record {
    string city;
    string? country;
};

type PostGradStudentArray PostGradStudent[];

json[] jStudentArr = [
    {
        "first_name": "Radha",
        "address": {
            "apartment_no": 123,
            "street": "Perera Mawatha",
            "city": "Colombo",
            "country": "Sri Lanka"
        },
        "employed": false
    },
    {
        "first_name": "Nilu",
        "last_name": "Peiris",
        "address": null,
        "employed": true
    },
    {
        "first_name": "Meena",
        "address": {
            "street": "Main Street",
            "city": "Colombo",
            "country": null
        },
        "employed": false
    }
];

function testFromJsonWithTypeWithNullValues() {
    json j1 = {x: null};
    IntVal val = checkpanic j1.fromJsonWithType(IntVal);
    assert(val, {x: ()});

    PostGradStudent[] studentArr = checkpanic jStudentArr.fromJsonWithType(PostGradStudentArray);
    assert(studentArr, [
        {
            employed: false,
            first_name: "Radha",
            address: {
                city: "Colombo",
                country: "Sri Lanka",
                apartment_no: 123,
                street: "Perera Mawatha"
            }
        },
        {employed: true, first_name: "Nilu", last_name: "Peiris", address: ()},
        {employed: false, first_name: "Meena", address: {city: "Colombo", country: (), street: "Main Street"}}
    ]);
}

function testFromJsonWithTypeWithNullValuesNegative() {
    json jVal = ();
    PostGradStudent|error val = jVal.fromJsonWithType(PostGradStudent);
    assertEquality(val is error, true);
    if (val is error) {
        assertEquality(val.message(), "{ballerina/lang.value}ConversionError");
        assertEquality(<string> checkpanic val.detail()["message"], "cannot convert '()' to type 'PostGradStudent'");
    }
}

function testFromJsonWithTypeWithInferredArgument() {
    json movie = {
        title: "Some",
        year: 2010
    };
    map<anydata> movieMap = checkpanic movie.fromJsonWithType();
    assertEquality(movieMap["title"], "Some");
    assertEquality(movieMap["year"], 2010);

    movieMap = checkpanic value:fromJsonWithType(v = movie);
    assertEquality(movieMap["title"], "Some");
    assertEquality(movieMap["year"], 2010);

    json arr = [1, 2];
    string[]|error s = arr.fromJsonWithType();
    assertEquality(s is error, true);
}

type FooBar [StringType...];

type StringType string;

public function testFromJsonWithTypeWithTypeReferences() {
    json j = ["foo"];
    FooBar f = checkpanic j.fromJsonWithType();
    assertEquality(f is FooBar, true);
    assertEquality(f is [string...], true);
    assertEquality(f.toString(), "[\"foo\"]");
}

type Student record {|
    string name;
    float grade = 10.0;
    boolean status?;
    int age;
    PermanentAddress address;
    string...;
|};

function testFromJsonWithTypeNestedRecordsNegative() {
    json j = {
        "name": "Radha",
        "age": 14,
        "address": {
            "apartment_no": 123,
            "street": "Perera Mawatha",
            "city": 7
        },
        "employed": false
    };

    (Student & readonly)|error radha = trap j.fromJsonWithType();

    error err = <error> radha;
    string errorMsg = "'map<json>' value cannot be converted to '(Student & readonly)': " +
    "\n\t\tmissing required field 'address.country' of type 'string?' in record '(PermanentAddress & readonly)'" +
    "\n\t\tfield 'address.city' in record '(PermanentAddress & readonly)' should be of type 'string', found '7'" +
    "\n\t\tvalue of field 'employed' adding to the record '(Student & readonly)' should be of type 'string', found 'false'";
    assertEquality(<string> checkpanic err.detail()["message"], errorMsg);
    assertEquality(err.message(), "{ballerina/lang.value}ConversionError");
}

/////////////////////////// Tests for `fromJsonStringWithType()` ///////////////////////////

function testFromJsonStringWithTypeJson() {
    string aNil = "()";
    string aNull = "null";
    string aString = "\"aString\"";
    string aNumber = "10";
    string anArray = "[\"hello\", \"world\"]";
    string anObject = "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}";
    string anInvalid = "{\"name\":\"anObject\",";
    map<json|error> result = {};

    result["aNil"] = aNil.fromJsonStringWithType(json);
    result["aNull"] = aNull.fromJsonStringWithType(json);
    result["aString"] = aString.fromJsonStringWithType(json);
    result["aNumber"] = aNumber.fromJsonStringWithType(json);
    result["anArray"] = anArray.fromJsonStringWithType(json);
    result["anObject"] = anObject.fromJsonStringWithType(json);
    result["anInvalid"] = anInvalid.fromJsonStringWithType(json);

    assertEquality(result["aNil"] is error, true);
    assertEquality(result["aNull"] is (), true);

    json aStringJson = <json> checkpanic result["aString"];
    assertEquality(aStringJson.toJsonString(), "\"aString\"");

    json anArrayJson = <json> checkpanic result["anArray"];
    assertEquality(anArrayJson.toJsonString(), "[\"hello\", \"world\"]");

    json anObjectJson = <json> checkpanic result["anObject"];
    assertEquality(anObjectJson.toJsonString(), "{\"name\":\"anObject\", \"value\":10, \"sub\":{\"subName\":\"subObject\", \"subValue\":10}}");

    assertEquality(result["anInvalid"] is error, true);
}

function testFromJsonStringWithTypeRecord() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    Student3|error studentOrError = str.fromJsonStringWithType(Student3);

    assertEquality(studentOrError is Student3, true);
    Student3 student = checkpanic studentOrError;
    assertEquality(student.name, "Name");
}

function testFromJsonStringWithAmbiguousType() {
    string str = "{\"name\":\"Name\",\"age\":35}";
    Student3|error p = str.fromJsonStringWithType(Student2Or3);
    assertEquality(p is error, false);
    assertEquality(p is Student2Or3, true);
    assertEquality(p is Student3, true);
}

function testFromJsonStringWithTypeMap() {
    string s = "{\"title\":\"Some\",\"year\":2010}";
    map<anydata> movieMap = checkpanic s.fromJsonStringWithType(MapOfAnyData);
    map<anydata> movieMap2 = <map<anydata>> movieMap;
    assertEquality(movieMap2["title"], "Some");
    assertEquality(movieMap2["year"], 2010);
}

function testFromJsonStringWithTypeStringArray() {
    string s = "[\"Hello\",\"World\"]";
    string[] a = checkpanic s.fromJsonStringWithType(StringArray);
    string[] a2 = <string[]> a;
    assertEquality(a2.length(), 2);
    assertEquality(a2[0], "Hello");
}

function testFromJsonStringWithTypeArrayNegative() {
    string s = "[1, 2]";
    string[]|error a = s.fromJsonStringWithType(StringArray);
    assertEquality(a is error, true);
}

function testFromJsonStringWithTypeIntArray() {
    string s = "[1, 2]";
    int[] arr = checkpanic s.fromJsonStringWithType(IntArray);
    int[] intArr = <int[]> arr;
    assertEquality(intArr[0], 1);
    assertEquality(intArr[1], 2);
}

function testFromJsonStringWithTypeWithInferredArgument() {
    string s = "[1, 2]";
    int[] arr = checkpanic s.fromJsonStringWithType();
    int[] intArr = <int[]> arr;
    assertEquality(intArr[0], 1);
    assertEquality(intArr[1], 2);

    string str = "{\"name\":\"Name\",\"age\":35}";
    Student3|error studentOrError = str.fromJsonStringWithType();
    assertEquality(studentOrError is Student3, true);
    Student3 student = checkpanic studentOrError;
    assertEquality(student.name, "Name");

    string arrStr = "[1, 2]";
    string[]|error a = value:fromJsonStringWithType(arrStr);
    assertEquality(a is error, true);
}

type OpenRecordWithUnionTarget record {|
    string|decimal...;
|};

function tesFromJsonWithTypeMapWithDecimal() {
    map<json> mp = {
        name: "foo",
        factor: 1.23d
    };
    var or = mp.fromJsonWithType(OpenRecordWithUnionTarget);

    if (or is error) {
        panic error("Invalid Response", detail = "Invalid type `error` recieved from cloneWithType");
    }

    OpenRecordWithUnionTarget castedValue = <OpenRecordWithUnionTarget> or;
    assertEquality(castedValue["factor"], mp["factor"]);
    assertEquality(castedValue["name"], mp["name"]);
}

public type Maps record {|int i;int...; |}|record {|int i?;|};

public type Value record {|
    Maps value;
|};

public function testConvertJsonToAmbiguousType() {
    json j = {"value": <map<int>> {i: 1}};
    Value|error res = j.cloneWithType(Value);
    assertEquality(res is error, false);
    assertEquality(res is Value, true);
    assertEquality(checkpanic res, <Value> checkpanic {value: {i: 1}});
}

type RegExpType string:RegExp;

function testFromJsonWithTypeOnRegExp() {
    string s = "AB+C*D{1,4}";
    RegExpType|error x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `AB+C*D{1,4}` == x1, true);
    }

    s = "A?B+C*?D{1,4}";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `A?B+C*?D{1,4}` == x1, true);
    }

    s = "A\\sB\\WC\\Dd\\\\";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `A\sB\WC\Dd\\` == x1, true);
    }

    s = "\\s{1}\\p{sc=Braille}*";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\s{1}\p{sc=Braille}*` == x1, true);
    }

    s = "AB+\\p{gc=Lu}{1,}";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `AB+\p{gc=Lu}{1,}` == x1, true);
    }

    s = "A\\p{Lu}??B+\\W\\(+?C*D{1,4}?";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `A\p{Lu}??B+\W\(+?C*D{1,4}?` == x1, true);
    }

    s = "\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*` == x1, true);
    }


    s = "[\\r\\n\\^]";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\r\n\^]` == x1, true);
    }

    s = "[A\\sB\\WC\\Dd\\\\]";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[A\sB\WC\Dd\\]` == x1, true);
    }

    s = "[\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA\\)]??";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA\)]??` == x1, true);
    }

    s = "[A\\sA-GB\\WC\\DJ-Kd\\\\]*";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[A\sA-GB\WC\DJ-Kd\\]*` == x1, true);
    }

    s = "[\\sA-F\\p{sc=Braille}K-Mabc-d\\--]";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\sA-F\p{sc=Braille}K-Mabc-d\--]` == x1, true);
    }

    s = "[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?` == x1, true);
    }

    s = "(?:ABC)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?:ABC)` == x1, true);
    }

    s = "(?i:ABC)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i:ABC)` == x1, true);
    }

    s = "(?i-m:AB+C*)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i-m:AB+C*)` == x1, true);
    }

    s = "(?imxs:AB+C*D{1,4})";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?imxs:AB+C*D{1,4})` == x1, true);
    }

    s = "(?imx-s:A?B+C*?D{1,4})";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?imx-s:A?B+C*?D{1,4})` == x1, true);
    }

    s = "(?i-s:\\s{1}\\p{sc=Braille}*)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i-s:\s{1}\p{sc=Braille}*)` == x1, true);
    }

    s = "(?ims-x:\\p{sc=Latin}\\p{gc=Lu}\\p{Lt}\\tA+?\\)*)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?ims-x:\p{sc=Latin}\p{gc=Lu}\p{Lt}\tA+?\)*)` == x1, true);
    }

    s = "(?im-sx:[A\\sA-GB\\WC\\DJ-Kd\\\\]*)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?im-sx:[A\sA-GB\WC\DJ-Kd\\]*)` == x1, true);
    }

    s = "(?i-sxm:[\\p{Lu}-\\w\\p{sc=Latin}\\p{gc=Lu}\\p{Lu}-\\w\\p{Lt}\\tA\\)\\p{Lu}-\\w]{12,32}?)";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?i-sxm:[\p{Lu}-\w\p{sc=Latin}\p{gc=Lu}\p{Lu}-\w\p{Lt}\tA\)\p{Lu}-\w]{12,32}?)` == x1, true);
    }

    s = "(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(?:(?i-m:ab|cd)|aa|abcdef[a-zefg-ijk-]|ba|b|c{1,3}^)+|ef` == x1, true);
    }

    s = "(z)((a+)?(b+)?(c))*";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `(z)((a+)?(b+)?(c))*` == x1, true);
    }

    s = "^^^^^^^robot$$$$";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `^^^^^^^robot$$$$` == x1, true);
    }

    s = "cx{0,93}c";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `cx{0,93}c` == x1, true);
    }

    s = "[\\d]*[\\s]*bc.";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `[\d]*[\s]*bc.` == x1, true);
    }

    s = "\\??\\??\\??\\??\\??";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\??\??\??\??\??` == x1, true);
    }

    s = ".?.?.?.?.?.?.?";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `.?.?.?.?.?.?.?` == x1, true);
    }

    s = "bc..[\\d]*[\\s]*";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `bc..[\d]*[\s]*` == x1, true);
    }

    s = "\\\\u123";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `\\u123` == x1, true);
    }

    s = "";
    x1 = s.fromJsonWithType(RegExpType);
    assertEquality(x1 is string:RegExp, true);
    if (x1 is string:RegExp) {
        assertEquality(re `` == x1, true);
    }
}

function testFromJsonWithTypeOnRegExpNegative() {
    string s = "AB+^*";
    RegExpType|error x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Invalid character '*'",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "AB\\hCD";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Invalid character '\\h'",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "AB\\pCD";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Invalid character '\\p'",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "AB\\uCD";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Invalid character '\\u'",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "AB\\u{001CD";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Invalid character '\\u{001CD'",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "AB\\p{sc=Lu";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Missing '}' character",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "[^abc";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Missing ']' character",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "(abc";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Missing ')' character",
        <string> checkpanic x1.detail()["message"]);
    }

    s = "(ab^*)";
    x1 = s.fromJsonWithType(RegExpType);
    assert(x1 is error, true);
    if (x1 is error) {
        assert("{ballerina/lang.value}ConversionError", x1.message());
        assert("'string' value cannot be converted to 'RegExpType': Failed to parse regular expression: Invalid character '*'",
        <string> checkpanic x1.detail()["message"]);
    }
}

type Assertion [string, string];

type BoundAssertion ["let", int];

type UnionTuple Assertion|BoundAssertion;

type Table1 table<map<int>>;

type Table2 table<map<string>>;

type UnionTable Table1|Table2;

function testFromJsonWithTypeToUnionOfTypeReference() {
    json[] arrValue = ["let", 3];

    BoundAssertion|error t1 = arrValue.fromJsonWithType(); 
    assertFalse(t1 is error);
    assertEquality(t1, <BoundAssertion> ["let", 3]);

    Assertion|BoundAssertion|error t2 = arrValue.fromJsonWithType(); 
    assertFalse(t2 is error);
    assertTrue(t2 is BoundAssertion);
    assertEquality(t2, <BoundAssertion> ["let", 3]);

    UnionTuple|error t3 = arrValue.fromJsonWithType(); 
    assertFalse(t3 is error);
    assertTrue(t3 is BoundAssertion);
    assertEquality(t3, <BoundAssertion> ["let", 3]);
}

function assert(anydata actual, anydata expected) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(reason);
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
