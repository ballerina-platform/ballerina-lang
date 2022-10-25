function testFunction(int i) returns (string) {
    transaction {
        if (i == 0) {
            rollback;
        } else {
            error? unionResult = commit;
            doSomething(unionResult);
        }
    }
    return "";
}

function doSomething(error? unionResult) {

}
