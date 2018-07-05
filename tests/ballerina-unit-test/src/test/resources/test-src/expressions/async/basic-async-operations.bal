import ballerina/runtime;
import ballerina/io;

int globalResult;

function testAsyncNonNativeBasic1() returns int {
  future<int> f1 = start add(5, 2);
  int result = await f1;
  return result;
}

function testAsyncNonNativeBasic2() returns int {
  future<int> f1 = start add(5, 2);
  int result = await f1;
  future<int> f2 = start add(10, 2);
  runtime:sleep(100);
  result = result + await f2;
  return result;
}

function testAsyncNonNativeBasic3() returns int {
  future<int> f1 = start add(5, 2);
  int result = await f1;
  future<int> f2 = start add(10, 2);
  result = result + await f2;
  result = result + await f2;
  return result;
}

function testAsyncNonNativeBasic4() returns int {
  future f1 = start addGlobal(1, 2);
  await f1;
  return globalResult;
}

function testAsyncNonNativeBasic5() returns float {
  future<float> f1 = start addSlow(10.5, 20.5);
  return await f1;
}

function testAsyncNonNativeBasic6() returns boolean {
  future<float> f1 = start addSlower(5, 5);
  boolean a = f1.isDone();
  float v1 = await f1;
  boolean b = f1.isDone();
  future f2 = start infiniteFunc();
  boolean c = f2.isCancelled();
  boolean d = f2.cancel();
  future f3 = start io:println("NATIVE ASYNC BLOCKING");
  boolean e = f3.cancel();
  future f4 = start runtime:sleep(100);
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
