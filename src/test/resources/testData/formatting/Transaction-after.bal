function testNestedTransactionWithFailed (int i) (string) {
    string a = "start";
    try {
        transaction with retries(3) {
            a = a + " inOuterTrx";
            transaction with retries(2) {
                a = a + " inInnerTrx";
                try {
                    if (i == -1) {
                        error err = {msg:" err"};
                        throw err;
                    } else if (i == 0) {
                        a = a + " abort";
                        abort;
                    } else if (i < -1) {
                        TrxError err = {msg:" trxErr", data:"test"};
                        throw err;
                    }
                } catch (TrxError err) {
                    a = a + err.msg;
                }
                a = a + " endInnerTrx";
            } failed {
                a = a + " innerFailed";
            }
            a = a + " endOuterTrx";
        } failed {
            a = a + " outerFailed";
        }
        a = a + " ";
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}
