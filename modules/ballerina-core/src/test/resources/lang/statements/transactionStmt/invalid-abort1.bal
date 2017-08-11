function test () {
    int i = 10;
    transaction {
        i = i + 1;
    } aborted {
        i = i + 10;
        abort;
    }
}