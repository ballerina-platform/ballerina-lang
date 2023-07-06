function demo() returns error? {
    retry<error:DefaultRetryManager>(3) transaction {
        check commit;
    }
}
