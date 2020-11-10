function trxWorkerTest1() returns int{
    int res = 0;
    transaction {
        res = foo();
        var s = commit;
    }

    return res + 1;
}

transactional function foo() returns int {
     int res = 0;
     transactional worker wx returns int {
        int x = 50;
        return x + 1;
     }
     res = wait wx;
     return res;
}
