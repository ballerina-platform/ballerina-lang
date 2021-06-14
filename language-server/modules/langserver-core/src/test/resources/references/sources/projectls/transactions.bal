function demo() returns error? {
    var manager = error:DefaultRetryManager;
    
    retry<error:DefaultRetryManager>(3) transaction {
        check commit;
    }
}
