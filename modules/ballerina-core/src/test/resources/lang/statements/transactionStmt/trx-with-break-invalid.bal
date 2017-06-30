function test () (string) {
    int i = 0;
    while (i < 5) {
        i = i + 1;
        transaction {
            if (i == 2) {
                break;
            }
        }
    }
    return "done";
}
