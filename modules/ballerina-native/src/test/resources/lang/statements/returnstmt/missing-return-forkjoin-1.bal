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

    } timeout (30000) (map msgs) {
        return null, null;
    }
}