function testNullInForkJoin () (message, message) {
    message m = null;
    fork {
        worker foo {
            message resp1 = null;
            resp1 -> fork;
        }

        worker bar {
            message resp2 = {};
            resp2 -> fork;
        }
    } join (all) (map allReplies) {
        any[] temp;
        temp,_ = (any[])allReplies["foo"];
        message m1;
        m1, _ = (message) temp[0];
        temp,_ = (any[])allReplies["bar"];
        message m2;
        m2, _ = (message) temp[0];
        return m1,m2;
    } timeout (30000) (map msgs) {

    }
}