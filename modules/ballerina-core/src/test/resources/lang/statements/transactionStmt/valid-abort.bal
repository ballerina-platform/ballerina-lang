function test ()(string) {
    string i = "st";
    transaction {
        transaction {
            i = i + " inTrx";
            abort;
        } aborted {
            i = i + " inAbt";
            abort;
        }
    } committed {
        i = i + " outCom";
    } aborted {
        i = i + " outAbt";
    }
    return i;
}

function testReturn1 ()(string) {
    string i = "st";
    transaction {
        i = i + " inTrx";
    } committed {
        i = i + " com";
        return i;
    }
    return "done";
}

function testReturn2 ()(string) {
    string i = "st";
    transaction {
        i = i + " inTrx";
        abort;
    } aborted {
        i = i + " abt";
        return i;
    }
    return "done";
}