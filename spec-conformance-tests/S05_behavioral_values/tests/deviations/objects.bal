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
import ballerina/test;

// If object-type-quals contains the keyword client, then the object type is a client
// object type. A client object type may have remote methods; other objects types must not.
// TODO: Can not declare client object without remote methods
//type ClientObject client object {
//
//};

// The names of all the methods of an object must be distinct: there is no method overloading.
// But the fields and methods of an object are in separate namespaces, so it is possible for an
// object to have a field and a method with the same name.
// TODO: Same name field and method should be allowed
//type DeviationObject object {
//    float sameNameFieldAndMethod = 12.1;
//
//    function sameNameFieldAndMethod() {
//
//    }
//};

// Visibility of fields, object initializer and methods is specified uniformly: public means that
// access is unrestricted; private means that access is restricted to the same object; if no
// visibility is specified explicitly, then access is restricted to the same module.
// TODO: Accessing private methods from inside the object type descriptor is not working.
//type AccessingPrivateMethodsBroken object {
//    private function privateMethod() returns string {
//        return "Success";
//    }
//
//    function defaultMethod() returns string {
//        return self.privateMethod();
//    }
//};
//
//@test:Config {}
//function testAccessingPrivateMethodsBroken() {
//    AccessingPrivateMethodsBroken obj = new;
//    test:assertEquals(obj.defaultMethod(), "Success", msg = "expected result by accessing private object method");
//}

// The return type of the __init method must be a subtype of the union of error and nil; if
// __init returns an error, it means that initialization of the object failed. The __init method
// can declare parameters in the same way as any other method.
// TODO: Init method of objects should be able to return a subset of error|().
//type InitMethodInObject object {
//    public function __init() returns error? {
//
//    }
//};

// object-field-descriptor := [visibility-qual] type-descriptor field-name ;
// TODO: Object fields should not be able to be initialized with a default value.
type NormalObjectBroken object {
    string fieldOne = "string value"; // Should fail at compile time
};

// At any point in the body of a __init method, the compiler determines which fields are
// potentially uninitialized. A field is potentially uninitialized at some point if it is a field of a type
// that does not have an implicit initial value and it is not definitely assigned at that point.
// TODO: Object fields not initialized in __init() should take its implicit initial value.
//type NormalObject2Broken object {
//    string fieldOne;
//    int[] fieldTwo;
//
//    function __init() {
//
//    }
//};
//
//@test:Config {}
//function testImplicitInitialValueOfObjectFields() {
//    NormalObject2Broken obj = new;
//    test:assertEquals(obj.fieldOne, "",
//        msg = "expected object field of type string to have its implicit initial value");
//    int[] expEmptyArray = [];
//    test:assertEquals(obj.fieldTwo, expEmptyArray,
//        msg = "expected object field of type int[] to have its implicit initial value");
//}
