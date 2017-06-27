function test () {
    int i = 10;
    transaction {
        i = i + 1;
    } committed {
        i = i + 10;
        abort;
    }
}