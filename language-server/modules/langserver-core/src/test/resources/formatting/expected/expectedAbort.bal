function name1() {
    transaction {
        abort;
    }
}

function name2() {
    transaction with retries = 0 {
        int b = 0;
        if (true) {
            abort;
        }
    } onretry {
        int a = 0;
    }
}
