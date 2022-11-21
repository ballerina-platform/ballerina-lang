// Copyright (c) (2019-2022), WSO2 Inc. (http://www.wso2.com).
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

import ballerina/lang.runtime;
import ballerina/jballerina.java;

int globalResult = 0;

function testAsyncNonNativeBasic1() returns int {
    future<int> f1 = start add(5, 2);
    int result = checkpanic wait f1;
    return result;
}

function testAsyncNonNativeBasic2() returns int {
    future<int> f1 = start add(5, 2);
    int result1 = checkpanic wait f1;
    future<int> f2 = start add(10, 2);
    sleep(100);
    int result2 = checkpanic wait f2;
    result1 = result1 + result2;
    return result1;
}

function testAsyncNonNativeBasic3() returns int {
    future<int> f1 = start add(5, 2);
    int result = checkpanic wait f1;
    future<int> f2 = start add(10, 2);
    int result2 = checkpanic  wait f2;
    result = result + result2;
    f2 = start add(10, 2);
    int result3 = checkpanic wait f2;
    result = result + result3;
    return result;
}

function testAsyncNonNativeBasic4() returns int {
    future<any> f1 = start addGlobal(1, 2);
    _ = checkpanic wait f1;
    return globalResult;
}

function testAsyncNonNativeBasic5() returns float {
    future<float> f1 = start addSlow(10.5, 20.5);
    return checkpanic wait f1;
}

function testAsyncNonNativeBasic6() returns boolean {
    future<float> f1 = start addSlower(5.0, 5.0);
    float v1 = checkpanic wait f1;
    future<any> f2 = start infiniteFunc();
    f2.cancel();
    future<any> f3 = start testNativeAsynchBlocking();
    f3.cancel();
    future<any> f4 = start sleep(100);
    f4.cancel();
    return v1 == 10.0;
}

function testNativeAsynchBlocking() {

}

function testAsyncNonNativeBasic7() returns int {
    future<int> f1 = start subtract(5, 2);
    f1.cancel();
    int result = checkpanic wait f1;
    return result;
}

function testAsyncNonNativeBasic8() returns int {
    future<int> f1 = @strand{thread:"parent"} start subtract(8, 3);
    future<int> f2 = @strand{thread:"parent"} start subtract(5, 2);
    f1.cancel();
    f2.cancel();
    int result = checkpanic wait f1 | f2;
    return result;
}

function testAsyncNonNativeBasic9() returns int {
    future<int> f1 = @strand{thread:"parent"} start subtract(5, 2);
    future<int> f2 = @strand{thread:"parent"} start addNum(5, 2);
    f1.cancel();
    int result = checkpanic wait f1 | f2;
    return result;
}

function testAsyncNonNativeBasic10() returns any {
    future<int> f1 = @strand{thread:"parent"} start addNum(5, 2);
    future<int> f2 = @strand{thread:"parent"} start subtract(5, 2);
    f2.cancel();
    record {| int|error f1; int|error f2; |} result = wait {f1,f2};
    return result;
}

function testAsyncNonNativeBasic11() returns any {
    future<int> f1 = @strand{thread:"parent"} start subtract(7, 2);
    future<int> f2 = @strand{thread:"parent"} start subtract(5, 2);
    f1.cancel();
    f2.cancel();
    record {| int|error f1; int|error f2; |} result = wait {f1,f2};
    return result;
}

function addNum(int i, int j) returns int {
    int l = 0;
    while (l < 10000) {
        l = l +1;
    }
    return i + j;
}

function subtract(int a, int b) returns int {
    infiniteFunc();
    return a - b;
}

function add(int a, int b) returns int {
    return a + b;
}

function addGlobal(int a, int b) {
    globalResult = a + b;
}

function addSlow(float a, float b) returns float {
    sleep(200);
    return a + b;
}

function addSlower(float a, float b) returns float {
    sleep(1200);
    return a + b;
}

function infiniteFunc() {
    int i = 0;
    while (true) {
        i = i + 1;
        runtime:sleep(0.0001);
    }
}

function testAsyncObjectAttachedFunctions() returns int {
    foo f = new;
    int a = f.doFoo(5);
    return a;
}

class foo {

    bar b = new;

    function doFoo(int x) returns int {
        future<int> f1 = start self.b.doBar(x);
        future<int> f2 = start self.doFoo1(x);
        int a = checkpanic wait f1;
        int b = checkpanic wait f2;
        return a + b;
    }

    function doFoo1(int x) returns int {
        return x;
    }
}

class bar {
    function doBar(int x) returns int {
        return x;
    }
}

public function testAsyncInvWithoutDefaultParams() returns int {
   future<int> aa = start asyncTest("example value!!!!!!!!");
   return checkpanic wait aa;
}

public function testAsyncInvWithDefaultParams() returns int {
   future<int> aa = start asyncTest("example value!!!!!!!!", e = 45);
   return checkpanic wait aa;
}

public function asyncTest(string a, int e = 5) returns int {
    return e;
}

Person p = new();

public function testAttachedAsyncInvWithoutDefaultParams() returns int {
   future<int> aa = start p.asyncTest("value");
   return checkpanic wait aa;
}

public function testAttachedAsyncInvWithDefaultParams() returns int {
   future<int> aa = start p.asyncTest("value", e = new);
   return checkpanic wait aa;
}

public class Person {

   public function asyncTest(string a, Emp e = new Emp(val = 40)) returns int {
       return e.val;
   }
}

public class Emp {
    public int val;
    public function init(int val = 9) {
        self.val = val;
    }
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
