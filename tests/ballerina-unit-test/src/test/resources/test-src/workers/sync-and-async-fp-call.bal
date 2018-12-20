import ballerina/io;
import ballerina/runtime;

int counter = 0;

function syncAsyncFpCall() returns boolean {
    var incrementFp = increment;
    any f;
    f = incrementFp.call();
    f = start incrementFp.call();
    return (counter == 1);
}


function increment() {
    runtime:sleep(50);
    counter = counter + 1;
}

