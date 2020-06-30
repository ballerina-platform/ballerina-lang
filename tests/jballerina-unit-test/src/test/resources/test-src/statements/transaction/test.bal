import ballerina/io;
function testCommit() {
    int|error x = actualCode(0, false);
    io:println(x);
}

function actualCode(int failureCutOff, boolean requestRollback) returns (int) {
    int i = 0;
    transaction {
       i += 1;
       transaction {
           i += 2;
           var x = commit;
       }
       var y = commit;
    }

    return i;
}