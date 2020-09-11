import ballerina/runtime;
import ballerina/io;

int globalResult = 0;

function testAsyncNonNativeBasic1() returns int {
    future<int> f1 = start add(5, 2);
    int result = wait f1;
    return result;
}

function testAsyncNonNativeBasic2() returns int {
    future<int> f1 = start add(5, 2);
    int result1 = wait f1;
    future<int> f2 = start add(10, 2);
    runtime:sleep(100);
    int result2 = wait f2;
    result1 = result1 + result2;
    return result1;
}

function testAsyncNonNativeBasic3() returns int {
    future<int> f1 = start add(5, 2);
    int result = wait f1;
    future<int> f2 = start add(10, 2);
    int result2 = wait f2;
    result = result + result2;
    int result3 = wait f2;
    result = result + result3;
    return result;
}

function testAsyncNonNativeBasic4() returns int {
    future<any> f1 = start addGlobal(1, 2);
    _ = wait f1;
    return globalResult;
}

function testAsyncNonNativeBasic5() returns float {
    future<float> f1 = start addSlow(10.5, 20.5);
    return wait f1;
}

function testAsyncNonNativeBasic6() returns boolean {
    future<float> f1 = start addSlower(5.0, 5.0);
    float v1 = wait f1;
    future<any> f2 = start infiniteFunc();
    f2.cancel();
    future<any> f3 = start io:println("NATIVE ASYNC BLOCKING");
    f3.cancel();
    future<any> f4 = start runtime:sleep(100);
    f4.cancel();
    return v1 == 10.0;
}

function testAsyncNonNativeBasic7() returns int {
    future<int> f1 = start subtract(5, 2);
    f1.cancel();
    int result = wait f1;
    return result;
}

function testAsyncNonNativeBasic8() returns int {
    future<int> f1 = start subtract(8, 3);
    future<int> f2 = start subtract(5, 2);
    f1.cancel();
    f2.cancel();
    int result = wait f1 | f2;
    return result;
}

function testAsyncNonNativeBasic9() returns int {
    future<int> f1 = start subtract(5, 2);
    future<int> f2 = start addNum(5, 2);
    f1.cancel();
    int result = wait f1 | f2;
    return result;
}

function testAsyncNonNativeBasic10() returns any {
    future<int> f1 = start addNum(5, 2);
    future<int> f2 = start subtract(5, 2);
    f2.cancel();
    any result = wait {f1,f2};
    return result;
}

function testAsyncNonNativeBasic11() returns any {
    future<int> f1 = start subtract(7, 2);
    future<int> f2 = start subtract(5, 2);
    f1.cancel();
    f2.cancel();
    any result = wait {f1,f2};
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
    runtime:sleep(200);
    return a + b;
}

function addSlower(float a, float b) returns float {
    runtime:sleep(1200);
    return a + b;
}

function infiniteFunc() {
    int i = 0;
    while (true) {
        i = i + 1;
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
        int a = wait f1;
        int b = wait f2;
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
   return wait aa;
}

public function testAsyncInvWithDefaultParams() returns int {
   future<int> aa = start asyncTest("example value!!!!!!!!", e = 45);
   return wait aa;
}

public function asyncTest(string a, int e = 5) returns int {
    return e;
}

Person p = new();

public function testAttachedAsyncInvWithoutDefaultParams() returns int {
   future<int> aa = start p.asyncTest("value");
   return wait aa;
}

public function testAttachedAsyncInvWithDefaultParams() returns int {
   future<int> aa = start p.asyncTest("value", e = new);
   return wait aa;
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
