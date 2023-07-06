transactional function workerReturnTest() returns int {
    @strand {thread: "any"}
    transactional worker wx returns int {
        int x = 50;
        return x + 1;
    }
    int res = wait wx;
    return res + 1;
}
