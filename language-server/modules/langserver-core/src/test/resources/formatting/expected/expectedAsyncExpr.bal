import ballerina/io;
import ballerina/runtime;

function test1() returns int {
    future<int> f1 = start add(5, 2);
    int result = await f1;
    return result;
}

function test2() returns int {
    future<int> f1 = start add(5, 2);
    int result = await f1;
    return result;
}

function test3() returns int {
    future<int> f1 = start add(5, 2);
    int result =
    await f1;
    return result;
}

function add(int a, int b) returns int {
    return a + b;
}
