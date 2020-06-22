import ballerina/runtime;
import ballerina/io;

const annotation v1 on source worker;
int globalResult = 0;

function testAsyncNonNativeBasic1() returns int {future<int> f1 = start add(5, 2);int result = wait f1;return result;}

function testAsyncNonNativeBasic2() returns int {
    future<int> f1 = start add(5, 2);
    int result = wait f1;
    future<int> f2 = start add(10, 2);
    runtime:sleep(100);
    result = result + wait f2;
    return result;
}

function testAsyncNonNativeBasic3() returns int {
  future<int> f1 = start add(5, 2);
        int result = wait f1;
    future<int> f2 = start add(10, 2);
          result = result + wait f2;
  result = result + wait f2;
    return result;
}

function testAsyncNonNativeBasic4() returns int {future<any> f1 = start addGlobal(1, 2);
        _ = wait f1;
    return globalResult;
}

function testAsyncNonNativeBasic5() returns float {
    future<float> f1 = start addSlow(10.5, 20.5);return wait f1;
}

function testAsyncNonNativeBasic6() returns boolean {
    future<float> f1 = start addSlower(5.0, 5.0);
    boolean a = f1.isDone();
float v1 = wait f1;
       boolean b = f1.isDone();
          future<any> f2 = start infiniteFunc();
   boolean c = f2.isCancelled();
      boolean d = f2.cancel();
       future<any> f3 = start io:println("NATIVE ASYNC BLOCKING");
            boolean e = f3.cancel();
 future<any> f4 = start runtime:sleep(100);
           boolean f = f4.cancel();
        return !a && v1 == 10.0 && b && !c && d && !e && !f;
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

public function main() {
    future<int> f1 =
             @v1
 start
           foo
 (
         )
 ;

    future<int> f2=       @v1       start    foo   ( ) ;
}

function foo() returns int {
    return 1;
}