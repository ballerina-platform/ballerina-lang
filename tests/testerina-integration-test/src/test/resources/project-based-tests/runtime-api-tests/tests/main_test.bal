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
import ballerina/jballerina.java;

public class TestPerson {

    public string name;
    public int age;
    public Address address;

    function init(string name, int age, Address address) {
        self.name = name;
        self.age = age;
        self.address = address;
    }
    public function getPersonCity() returns string {
        return self.address.city;
    }

    public function play(string sport = "cricket") returns string {
        return sport;
    }

    public function callPlayWithArgs(string s) returns string = @java:Method {
        'class: "org.ballerinalang.testerina.utils.RuntimeApi"
    } external;

    public function callPlayWithoutArgs() returns string = @java:Method {
        'class: "org.ballerinalang.testerina.utils.RuntimeApi"
    } external;
}

public type TestAddress record {
    string city;
    string country;
    int postalCode;
};

public type TestGenericError distinct error;

public function getRecord(string recordName) returns record{} = @java:Method {
    'class: "org.ballerinalang.testerina.utils.RuntimeApi"
} external;

public function getTestRecord(string recordName) returns record{} = @java:Method {
    'class: "org.ballerinalang.testerina.utils.RuntimeApi"
} external;

public function getObject(string objectName) returns object{} = @java:Method {
    'class: "org.ballerinalang.testerina.utils.RuntimeApi"
} external;

public function getTestObject(string objectName) returns object{} = @java:Method {
    'class: "org.ballerinalang.testerina.utils.RuntimeApi"
} external;

public function getError(string errorName) returns error = @java:Method {
    'class: "org.ballerinalang.testerina.utils.RuntimeApi"
} external;

@test:Config {}
function testRuntimeApi() {
    Person p1 = <Person> getObject("Person");
    TestPerson p2 = <TestPerson> getTestObject("TestPerson");
    Address address1 = <Address> getRecord("Address");
    Address address2 = <TestAddress> getTestRecord("TestAddress");
    test:assertEquals(p1.getPersonCity(), "Colombo");
    test:assertEquals(p2.getPersonCity(), "Kandy");
    test:assertEquals(address1.city, "Colombo");
    test:assertEquals(address2.city, "Kandy");
    error userError1 = getError("GenericError");
    error userError2 = getError("TestGenericError");
    test:assertTrue(userError1 is GenericError);
    test:assertTrue(userError2 is TestGenericError);
    test:assertEquals(p2.callPlayWithArgs("football"), "football");
    test:assertEquals(p2.callPlayWithoutArgs(), "cricket");
}
