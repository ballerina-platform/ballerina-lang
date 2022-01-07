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

function testFromXML() returns json|error {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>supun</name>`;
    xml x3 = x1 + x2;
    json|error j = jsonutils:fromXML(x3);
    return j;
}

// Added a temporary fix due to following issue
// https://github.com/ballerina-platform/ballerina-standard-library/issues/2559
public function testFromTable() returns string {
    table<Person> personTable = table{
        { key id, age, salary, name, married },
        [ { 1, 30,  301, "Mary", true },
          { 2, 20,  301, "John", true }
        ]
    };

    return jsonutils:fromTable(personTable).toJsonString();
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

function testXMLWithEmptyChildren() returns string {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    json j = checkpanic jsonutils:fromXML(x);
    return j.toJsonString();
}
