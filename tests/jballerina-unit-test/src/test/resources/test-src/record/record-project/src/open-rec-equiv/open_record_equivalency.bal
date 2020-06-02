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

import eq;
import eq2;
import req;
import req2;

public type person1 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
};

public type employee1 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    int employeeId = 123456;
};

function testEquivalenceOfPrivateStructsInSamePackage () returns string {
    employee1 e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    person1 p = e;

    return p.ssn;
}

public type person2 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
};

public type employee2 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    int employeeId = 123456;
};

function testEquivalenceOfPublicStructsInSamePackage () returns string {
    employee2 e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    person2 p = e;

    return p.ssn;
}


function testEqOfPublicStructs () returns string {
    eq:employee e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    eq:person p = e;

    return p.ssn;
}


public type employee3 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    int employeeId = 123456;
};

function testEqOfPublicStructs1 () returns string {
    employee3 e = {age:14, name:"rat"};
    e.ssn = "234-56-1234:employee";

    eq:person p = e;

    return p.ssn;
}

function testEqOfPublicStructs2 () returns string {
    eq2:employee e = {age:14, name:"rat"};
    e.ssn = "234-56-3345:employee";

    eq:person p = e;

    return p.ssn;
}




type userA record {
    int age = 0;
    string name = "";
};

type userB record {
    int age = 0;
    string name = "";
    string address = "";
};

type userFoo record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
};


function testRuntimeEqPrivateStructsInSamePackage () returns string|error {
    userFoo uFoo = {age:10, name:"ttt", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    userB uB = uFoo;

    userA uA = uB;
    return uA.name;
}


public type userPA record {
    int age = 0;
    string name = "";
};

public type userPB record {
    int age = 0;
    string name = "";
    string address = "";
};


public type userPFoo record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
};

function testRuntimeEqPublicStructsInSamePackage () returns string|error {
    userPFoo uFoo = {age:10, name:"Skyhigh", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    userPA uA = uFoo;

    var uB = check uA.cloneWithType(userPB);
    return uB.name;
}

function testRuntimeEqPublicStructs () returns string|error {
    req:userPFoo uFoo = {age:10, name:"Skytop", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    userPA uA = uFoo;

    // This is a unsafe cast
    var uB  = check uA.cloneWithType(userPB);
    return uB.name;
}

function testRuntimeEqPublicStructs1 () returns string|error {
    req:userPFoo uFoo = {age:10, name:"Brandon", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    userPA uA = uFoo;

    var uB  = check uA.cloneWithType(req2:userPB);
    return uB.name;
}

type Foo record {
    string a = "";
    string b = "";
    string c = "";
};

type AnotherFoo record {
    string c = "";
    string b = "";
    string a = "";
    int d = 0;
    float e = 0.0;
    person1? p = ();
};

function testRecordEquivalence() returns Foo {
    AnotherFoo af = {a: "A", b: "B", c: "C", d: 10};
    af["f"] = "rest field";
    Foo f = af;
    return f;
}

function testUnorderedFieldRecordsInAMatch() returns Foo? {
    AnotherFoo|string aFoo = {a: "A", b: "B", c: "C", d: 10, "f": "rest field"};

    if aFoo is AnotherFoo {
        return aFoo;
    }
    return ();
}
