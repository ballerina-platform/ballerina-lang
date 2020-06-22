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

function name3() {
    transaction with retries = 0 {
        int h = 0;
    } onretry {
        a = a + " retry";
    } aborted {
        a = a + " committed";
    } committed {
        a = a + " aborted";
    }
}

function name4() {
    transaction with retries = 0 {
        int h = 0;
    }
    onretry {
        a = a + " retry";
    }
    committed {
        a = a + " committed";
    }
    aborted {
        a = a + " aborted";
    }
}
