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

import ballerina/jsonutils;

type Person record {
    int id;
    int age = -1;
    decimal salary;
    string name;
    boolean married;
};

type Employee record {
    int id;
    string name;
    float salary;
    boolean permanent;
    string[] dependents;
    Contact contact;
};

type Contact record {
    int[] phone;
    Address address;
    string emergency;
};

type Address record {
    int number;
    string street;
};

function testFromXML() returns json|error {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>supun</name>`;
    xml x3 = x1 + x2;
    json|error j = jsonutils:fromXML(x3);
    return j;
}

public function testFromTable() returns string {
    table<Person> personTable = table[ { id: 1, age: 30,  salary: 300.5, name: "Mary", married: true },
          { id: 2, age: 20,  salary: 300.5, name: "John", married: true }
        ];

    return jsonutils:fromTable(personTable).toJsonString();
}

public function testFromTable2() returns string {
    table<Employee> employeeTable = table [
                {id: 1, name: "Mary", salary: 300.5, permanent: true, dependents: ["Mike", "Rachel"],
                    contact: {
                        phone: [445566, 778877],
                        address: {number: 34, street: "Straford"},
                        emergency: "Stephen"}},
                {id: 2, name: "John", salary: 200.5, permanent: false, dependents: ["Kyle"],
                    contact: {
                        phone: [6060606, 556644],
                        address: {number: 10, street: "Oxford"},
                        emergency: "Elizabeth"}} ,
                {id: 3, name: "Jim", salary: 330.5, permanent: true, dependents: [],
                    contact: {
                        phone: [960960, 889889],
                        address: {number: 46, street: "Queens"},
                        emergency: "Veronica"}}
            ];

    return jsonutils:fromTable(employeeTable).toJsonString();
}

function testFromXML2() returns json|error {
    return jsonutils:fromXML(xml `foo`);
}

xml e = xml `<Invoice xmlns="example.com" attr="attr-val" xmlns:ns="ns.com" ns:attr="ns-attr-val">
        <PurchesedItems>
            <PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>
            <PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>
            <PLine><ItemCode discount="22%">200777</ItemCode><Count>7</Count></PLine>
        </PurchesedItems>
        <Address xmlns="">
            <StreetAddress>20, Palm grove, Colombo 3</StreetAddress>
            <City>Colombo</City>
            <Zip>00300</Zip>
            <Country>LK</Country>
        </Address>
    </Invoice>`;

function testComplexXMLElementToJson() returns string {
    json j = checkpanic jsonutils:fromXML(e);
    return j.toJsonString();
}

function testComplexXMLElementToJsonNoPreserveNS() returns string {
    json j = checkpanic jsonutils:fromXML(e, { preserveNamespaces: false });
    return j.toJsonString();
}

function testUsingConvertedJsonValue() returns string {
    json j = checkpanic jsonutils:fromXML(xml `<Element><A>BCD</A><A>ZZZ</A></Element>`);
    json[] ar = <json[]>(checkpanic j.Element.A);
    return (<string> ar[0]) + ":" + (<string> ar[1]);
}

type PInfo record {
    string name;
    string age;
    string gender;
};

function testXmlToJsonToPInfo() returns PInfo {
    json j = checkpanic jsonutils:fromXML(
        xml `<PInfo><name>Jane</name><age>33</age><gender>not-specified</gender></PInfo>`);
    json k = checkpanic j.PInfo;
    PInfo p = checkpanic k.cloneWithType(PInfo);
    return p;
}

function testXMLWithEmptyChildren() returns string {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    json j = checkpanic jsonutils:fromXML(x);
    return j.toJsonString();
}

function testXMLToJosnArray() {
    xml x = xml `<Root><A/><B/><C/></Root>`;
    json j = checkpanic jsonutils:fromXML(x);
    json expected = {"Root": {"A":"", "B":"", "C":""}};
    assertValueEquality(expected, j);
}

function testXMLSameKeyToJosnArray() {
    xml x = xml `<Root><A>A</A><A>B</A><A>C</A></Root>`;
    json j = checkpanic jsonutils:fromXML(x);
    json expected = {"Root": {"A":["A", "B", "C"]}};
    assertValueEquality(expected, j);
}

function testXMLSameKeyWithAttrToJsonArray() {
    xml x = xml `<Root><A attr="hello">A</A><A attr="name">B</A><A>C</A></Root>`;
    json j = checkpanic jsonutils:fromXML(x);
    json expected = {"Root": [{"A":"A", "@attr":"hello"}, {"A":"B", "@attr":"name"}, {"A": "C"}]};
    assertValueEquality(expected, j);
}

function testXMLElementWithMultipleAttributesAndNamespaces() {
    xml x = xml `<Root xmlns:ns="ns.com" ns:x="y" x="z"/>`;
    json j = checkpanic jsonutils:fromXML(x);
    json expected = {"Root": {"@xmlns:ns":"ns.com", "@ns:x":"y", "@x":"z"}};
    assertValueEquality(expected, j);
}

function assertValueEquality(json expected, json actual) {
    if expected == actual {
        return;
    }

    panic error("Assertion error",
                message = "expected '" + expected.toJsonString() + "', found '" + actual.toJsonString () + "'");
}
