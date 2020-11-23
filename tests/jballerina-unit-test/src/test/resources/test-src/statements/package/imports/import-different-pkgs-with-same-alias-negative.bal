import ballerina/lang.'int as x;
import ballerina/lang.'string as x;

function testFunc() {
    int|error i1 = x:fromString("100");
}
