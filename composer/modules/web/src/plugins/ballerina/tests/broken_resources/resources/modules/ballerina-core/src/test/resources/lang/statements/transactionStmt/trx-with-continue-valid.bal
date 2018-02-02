function test () (string) {
    int i = 0;
    transaction {
        while (i < 5) {
            i = i + 1;
            if (i == 2) {
                continue;
            }
        }
    }
    return "done";
}

function test2 () (string) {
    int i = 0;
    while (i < 5) {
        transaction {
            i = i + 1;
        } committed {
            if (i == 2) {
                continue;
            }
        }
    }
    return "done";
}

function test3 () (string) {
    int i = 0;
    while (i < 5) {
        transaction {
            i = i + 1;
            abort;
        } aborted {
            if (i == 2) {
                continue;
            }
        }
    }
    return "done";
}
