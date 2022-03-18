// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;
import testOrg/configLib.util;

type Student record {
    string name;
    int age;
    string[] subjects;
    map<int> marks;
};

type Person record {|
    string name;
    int...;
|};

public enum CountryCodes {
    SL = "Sri Lanka",
    US = "United States"
}

// Tuple with simple types
configurable [int, string, float, decimal, byte, boolean] simpleTuple = ?;
// configurable [xml, xml:Comment, xml:Element, xml:ProcessingInstruction, xml:Text] xmlTuple = ?;
configurable [int[], string[][], int[][][]] arrayTuple = ?;
configurable [string, map<anydata>] mapTuple = ?;
configurable [int, Student] recordTuple1 = ?;
configurable [int, Student] recordTuple2 = ?;
configurable [int, Person] recordTuple3 = ?;
configurable [string, table<map<anydata>>] tableTuple1 = ?;
configurable [string, table<Student>] tableTuple2 = ?;
configurable [int, CountryCodes] enumTuple = ?;
configurable [int, int|string] unionTuple1 = ?;
configurable [int, int|map<int>] unionTuple2 = ?;
configurable [string, map<string>[]] mapArrayTuple = ?;
configurable [string, Student[]] recordArrayTuple = ?;

public function main() {
    testTuples();
    util:print("Tests passed");
}

function testTuples() {
    test:assertEquals(simpleTuple.toString(), "[278,\"string\",2.3,4.5,2,true]");
    test:assertEquals(arrayTuple.toString(), "[[1,3,5,7,9],[[\"apple\",\"banana\",\"orange\"],[\"ten\",\"twenty\"," +
    "\"thirty\"]],[[[1,2],[3,4]],[[1,2],[3,4]]]]");
    // Need to enable this after fixing issue #35395
    // test:assertEquals(xmlTuple.toString(), "[[1,3,5,7,9],[[\"apple\",\"banana\",\"orange\"]," 
    // "[\"ten\",\"twenty\",\"thirty\"]]]");
    test:assertEquals(mapTuple.toString(), "[\"Foo\",{\"address\":{\"number\":20,\"road\":\"Palm Grove\"," +
    "\"city\":\"Colombo 3\"},\"name\":\"Baz Qux\",\"accounts\":[{\"id\":\"1\"},{\"id\":\"2\"}]," +
    "\"salary\":[45000.0,150000.0],\"age\":22}]");
    test:assertEquals(recordTuple1.toString(), "[1,{\"name\":\"John Doe\",\"age\":22,\"subjects\":[\"English\"," +
    "\"Science\"],\"marks\":{\"English\":85,\"Science\":90}}]");
    test:assertEquals(recordTuple2.toString(), "[2,{\"name\":\"John Doe\",\"age\":22,\"subjects\":[\"English\"," +
    "\"Science\"],\"marks\":{\"English\":85,\"Science\":90},\"weight\":70,\"height\":158.4}]");
    test:assertEquals(recordTuple3.toString(), "[3,{\"name\":\"John Doe\",\"weight\":67,\"age\":22,\"height\":177}]");
    test:assertEquals(tableTuple1.toString(), "[\"Bar\",[{\"a\":\"a\"},{\"a\":1},{\"a\":2.34},{\"a\":false}," +
    "{\"a\":[1,2]},{\"a\":{\"b\":\"a\",\"c\":12}},{\"a\":[{\"b\":\"bb\"},{\"c\":\"cc\"}]}]]");
    test:assertEquals(tableTuple2.toString(), "[\"Hello\",[{\"name\":\"John Doe\",\"age\":22,\"subjects\":" +
    "[\"English\",\"Science\"],\"marks\":{\"English\":85,\"Science\":90}},{\"name\":\"Jane Doe\",\"age\":27," +
    "\"subjects\":[\"Maths\",\"History\"],\"marks\":{\"Maths\":88,\"History\":97}}]]");
    test:assertEquals(enumTuple.toString(), "[33,\"Sri Lanka\"]");
    test:assertEquals(unionTuple1.toString(), "[123,\"Hello World\"]");
    test:assertEquals(unionTuple2.toString(), "[456,{\"a\":1,\"b\":2}]");
    test:assertEquals(mapArrayTuple.toString(), "[\"Jane\",[{\"subject\":\"English\",\"name\":\"John Doe\"," +
    "\"age\":\"22\"},{\"subject\":\"Maths\",\"name\":\"Jane Doe\",\"age\":\"27\"}]]");
    test:assertEquals(recordArrayTuple.toString(), "[\"John\",[{\"name\":\"John Doe\",\"age\":22," +
    "\"subjects\":[\"English\",\"Science\"],\"marks\":{\"English\":85,\"Science\":90}},{\"name\":\"Jane Doe\"," +
    "\"age\":27,\"subjects\":[\"Maths\",\"History\"],\"marks\":{\"Maths\":88,\"History\":97}}]]");
}
