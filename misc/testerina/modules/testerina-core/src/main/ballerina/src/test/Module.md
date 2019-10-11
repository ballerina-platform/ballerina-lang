## Module overview

This module allow developers to write testable code.

## Samples

Following sample shows how to use assertions in Testerina.

```ballerina

import ballerina/test;

@test:Config{}
function testAssertIntEquals() {
    int answer = 0;
    int a = 5;
    int b = 3;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, msg = "int values not equal");
}

function intAdd(int a, int b) returns (int) {
    return (a + b);
}
```
