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

public type ServiceOne service object {

};

public type ServiceTwo service object {

};

public type Obj object {

};

function testSerivceObjectAssignability() {
    any i = 0;
    ServiceOne k = <ServiceOne> i;
    Obj q = k;
    ServiceTwo p = k;
    ServiceTwo pp = q;

    ServiceWithSingleMethod singleMethod = k;
    ServiceOne k1 = singleMethod;

    ObjWithSingleMethod objSingleMethod = singleMethod;
}

public type ServiceWithSingleMethod service object {
    public function foo(int i) returns int;
};

public type ObjWithSingleMethod object {
    public function foo(int i) returns int;
};

public type ServiceWithRemoteMethod service object {
    remote function foo(int i) returns int;
};

public type ServiceWithRemoteMethodTwo service object {
    remote function foo(int i) returns int;
};

function testAssignabilityWithRemoteMethods() {
    any i = 0;
    ServiceWithRemoteMethod k = <ServiceWithRemoteMethod> i;
    ObjWithSingleMethod q = k;
    ServiceWithRemoteMethodTwo p = k;
}

public type DualAccessorService service object {
    resource function get resMethod() returns string;
    resource function post resMethod() returns string;
};

public type SingleAccessorService service object {
    resource function get resMethod() returns string;
};

function testAssessorAssignability(SingleAccessorService s0, DualAccessorService s1) {
    SingleAccessorService s0_0 = s0;
    SingleAccessorService s0_1 = s1;
    DualAccessorService s1_0 = s0;
    DualAccessorService s1_1 = s1;
}
