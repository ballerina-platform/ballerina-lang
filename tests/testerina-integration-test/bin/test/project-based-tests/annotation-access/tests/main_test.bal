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

import ballerina/test;

@ann {
    id: 1
}
type Employee record {
    int id;
};

annotation testAnnot on function,type;

function foo() {
}

@testAnnot
type Bar object {

};

@test:Config {}
function testTestConstructSrcAnnotAccess() {
    Employee e = {
        id: 4567
    };

    typedesc<Employee> td = typeof e;
    test:assertEquals(td.@ann?.id, 1);
}

@test:Config {}
function testTestConstructTestAnnotAccess() {
    typedesc<any> tdFoo = typeof foo;
    test:assertEquals(tdFoo.@testAnnot, ());

    typedesc<any> tdBar = Bar;
    test:assertEquals(tdBar.@testAnnot, true);
}

@test:Config {}
function testTestSrcConstructSrcAnnotAccess() {
    test:assertEquals(getFooAnnotId(), 100);
}

type MyAnnotation record {
    string foo;
};

annotation MyAnnotation serviceAnnotation on service, class;

service object {} ser =
@serviceAnnotation{
    foo: "serviceAnnotation"
}
service object {
    int i = 1234;

    remote function xyz() {

    }
};

@test:Config {}
public function testServiceAnnotReordering() {
    typedesc<any> td = typeof ser;

    MyAnnotation? x = td.@serviceAnnotation;
    test:assertTrue(x is MyAnnotation);
    test:assertEquals(x, <MyAnnotation> {foo: "serviceAnnotation"});
}
