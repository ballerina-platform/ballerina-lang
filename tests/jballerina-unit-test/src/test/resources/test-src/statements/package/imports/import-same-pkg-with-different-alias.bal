import ballerina/lang.'int;
import ballerina/lang.'int as x;
import ballerina/lang.'int as y;

function testFunc() {
    int|error i1 = 'int:fromString("100");
    int|error i2 = x:fromString("100");
    int|error i3 = y:fromString("100");
}
