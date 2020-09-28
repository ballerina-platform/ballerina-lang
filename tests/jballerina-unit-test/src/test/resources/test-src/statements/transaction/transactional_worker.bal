function workerReturnTest() returns int{

    transaction {
        //res = foo();
        transactional worker wx returns int {
            int res = 0;
            	        int x = 50;
            	        return x + 1;
                    }
                    res = wait wx;
        var s = commit;
    }
    return 1 + 1;
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
