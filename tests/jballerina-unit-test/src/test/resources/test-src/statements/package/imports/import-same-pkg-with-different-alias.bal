import ballerina/lang.'int;
import ballerina/lang.'int as x;
import ballerina/lang.'int as y;

function testFunc() returns error? {
    int _ = check 'int:fromString("100");
    int _ = check x:fromString("100");
    int _ = check y:fromString("100");
}
