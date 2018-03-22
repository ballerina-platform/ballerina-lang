import ballerina/runtime;

int globalResult;

function testAsyncNonNativeBasic1() returns int {
  future<int> f1 = async add(5, 2);
  int result = await f1;
  return result;
}

function testAsyncNonNativeBasic2() returns int {
  future<int> f1 = async add(5, 2);
  int result = await f1;
  future<int> f2 = async add(10, 2);
  runtime:sleepCurrentWorker(100);
  result = result + await f2;
  return result;
}

function testAsyncNonNativeBasic3() returns int {
  future<int> f1 = async add(5, 2);
  int result = await f1;
  future<int> f2 = async add(10, 2);
  result = result + await f2;
  result = result + await f2;
  return result;
}

function testAsyncNonNativeBasic4() returns int {
  future f1 = async addGlobal(1, 2);
  await f1;
  return globalResult;
}

function testAsyncNonNativeBasic5() returns float {
  future<float> f1 = async addSlow(10.5, 20.5);
  return await f1;
}

function add(int a, int b) returns int {
  return a + b;
}

function addGlobal(int a, int b) {
  globalResult = a + b;
}

function addSlow(float a, float b) returns float {
  runtime:sleepCurrentWorker(200);
  return a + b;
}
