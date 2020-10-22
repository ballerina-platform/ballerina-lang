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

import ballerina/java;

public class Person {

    string name;

    int age;

    float height;

    public function init(string name, int age, float height) {
      self.name = name;
      self.age = age;
      self.height = height;
    }

    function newInstance() returns handle = @java:Constructor {
        'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
    } external;

    public function getCounter(handle receiver) returns handle = @java:Method{
        'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
    } external;

    public function setCounterValue(handle receiver, handle count) = @java:Method{
        'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
    } external;

    public function getObjectValueField(handle receiver) returns int = @java:Method{
        'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
    } external;

    public function getInt(handle h, int x) returns int = @java:Method{
        'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
    } external;

    public function getRandomInt(handle h) returns int = @java:Method{
        'class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
    } external;

    public function acceptTwoParamsAndReturnSomething(handle s, handle s2) returns handle = @java:Method {
       'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;

    public function acceptObjectAndObjectReturn(int age) returns Person = @java:Method {
       'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;

    public function acceptObjectAndReturnField() returns int = @java:Method {
       'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;

    function echoObject() returns object {}  = @java:Method {
            'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;

    function getBIntFromJInt(handle receiver) returns int = @java:Method {
        name:"longValue",
        'class:"java.lang.Integer"
    } external;

    function newInteger(int value) returns handle = @java:Constructor {
        'class:"java.lang.Integer"
    } external;

     public function floor(float a) returns float = @java:Method {
             'class: "java/lang/Math"
     } external;
}

function getBIntFromJInt(handle receiver) returns int = @java:Method {
    name:"longValue",
    'class:"java.lang.Integer"
} external;

public function testInteropsInsideObject() {
    Person p = new("Waruna", 5, 123.45);

    // Test instance java interop methods inside object
    handle h = p.newInstance();
    int counter = p.getBIntFromJInt(p.getCounter(h));
    assertEquality(counter, 0);
    p.setCounterValue(h, p.newInteger(24));
    counter = p.getBIntFromJInt(p.getCounter(h));
    assertEquality(counter, 24);
    int age = p.getObjectValueField(h);
    assertEquality(age, 5);
    int x = p.getInt(h, 444);
    assertEquality(x, 5);
    int y = p.getRandomInt(h);
    assertEquality(y, 123);

    // Test static java interop methods inside object
    string message = <string>java:toString(p.acceptTwoParamsAndReturnSomething(java:fromString("Hello "),
                                                                               java:fromString("World")));
    assertEquality(message, "Hello World");
    age = p.acceptObjectAndReturnField();
    assertEquality(age, 5);
    Person samePerson = p.acceptObjectAndObjectReturn(12);
    assertEquality(p.age, 12);
    assertEquality(samePerson.age, 12);
    Person p1 = <Person>p.echoObject();
    assertEquality(p.name, "Waruna");
    assertEquality(p.age, 12);
    assertEquality(p.height, 123.45);
    float f = p.floor(p.height);
    assertEquality(f, 123.0);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "found '" + expected.toString() + "', expected '" + actual.toString () + "'");
}
