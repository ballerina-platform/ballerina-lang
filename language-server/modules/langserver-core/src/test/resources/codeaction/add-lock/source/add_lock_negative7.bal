isolated future<int>[] asyncArr = [];

isolated function name() {
    asyncArr[0] = start asyncFn();
}

isolated function asyncFn() returns int => 0;
