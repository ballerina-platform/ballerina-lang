import ballerina/lang.'transaction as trx;

string x = "";
function testSetRollbackOnly(int i, boolean setRollbackOnly) returns string|error {

    transaction {
        x += " In Trx";
        check commit;
        x += " commit";
    }
    setRollbackOnlyError();
    return x;
}

transactional function setRollbackOnlyError() {
    error cause = error("setRollbackOnly");
    trx:setRollbackOnly(cause);
}
