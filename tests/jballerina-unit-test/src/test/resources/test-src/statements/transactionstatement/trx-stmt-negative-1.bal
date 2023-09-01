function dummyTest1() returns error? {
    transaction {
     do {
        check commit;
        check foo();
     } on fail {
        check commit; // Error: commit is not allowed here
     }
    }
}

function dummyTest2() returns error? {
    transaction {
     do {
        check commit;
        check foo();
     } on fail {
     }
    }
}

function dummyTest3() returns error? {
    transaction { // Error: transaction exit prior to commit/rollback identified]
     do {
        check foo();
        check commit;
     } on fail {
     }
    }
}

function dummyTest() returns error? {
    transaction {
     do {
        check foo();
        check commit;
     } on fail {
        rollback;
     }
    }
}

function dummyTest() returns error? {
    transaction { // invalid transaction commit count
                  // transaction exit prior to commit/rollback identified
     do {
        check foo();
     } on fail {
     }
    }
}

function dummyTest() returns error? {
    transaction {
     do {
        check foo();
     } on fail {
     }
     check commit;
    }
}

function dummyTest() returns error? {
    transaction { // invalid transaction commit count
     do {
        check foo();
     } on fail {
     }
     rollback;
    }
}