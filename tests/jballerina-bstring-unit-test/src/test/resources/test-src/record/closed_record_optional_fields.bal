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

type CountryCode "LK"|"USA"|"UK";

type Person record {|
    string fname = "default";
    string lname?;
    Address adrs = {};
|};

type Address record {|
    string street = "";
    string city = "";
    CountryCode country = "LK";
|};

function testNonDefReqField() returns Person {
    Person p = {adrs:{country: "LK"}};
    return p;
}

function testNonDefReqField2() returns Person {
    Person p = {fname: "John", lname: "Doe", adrs:{country: "LK"}};
    return p;
}

// When adrs is a defaultable field
type Person2 record {|
    string fname = "default";
    string lname?;
    Address2 adrs = {};
|};

type Address2 record {|
    string street = "";
    string city = "";
    CountryCode country = "LK";
|};

function testDefaultableReqField() returns Person2 {
    Person2 p = {};
    return p;
}

// When adrs is an optional field
type Person3 record {|
    string fname = "default";
    string lname?;
    Address3 adrs?;
|};

type Address3 record {|
    string street = "";
    string city = "";
    CountryCode country = "LK";
|};

function testOptionalNonDefField() returns Person3 {
    Person3 p = {};
    return p;
}

function testOptionalNonDefField2() {
    Person3 p = {};
    Address3 a = <Address3>p?.adrs;
    Address3 b = <Address3>p.get("adrs");
}

function testOptionalNonDefField3() {
    Person3 p = {};
    anydata b = p.get("adrs");
}

function testOptionalNonDefField4() returns [Address3, Address3] {
    Person3 p = {adrs:{street: "Palm Grove", city: "Colombo 3"}};
    Address3 a = <Address3>p.get("adrs");
    Address3 b = <Address3>p?.adrs;
    return [a, b];
}

// When adrs is an optional defaultable field
type Person4 record {|
    string fname = "default";
    string lname?;
    Address4 adrs?;
|};

type Address4 record {|
    string street = "";
    string city = "";
    CountryCode country = "LK";
|};

function testOptionalDefaultableField() returns Person4 {
    Person4 p = {};
    return p;
}
