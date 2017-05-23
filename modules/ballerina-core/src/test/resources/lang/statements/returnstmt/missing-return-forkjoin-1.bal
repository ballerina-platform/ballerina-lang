function testNullInForkJoin () (message, message) {
    message m = null;
    fork {
        worker foo {
            message resp1 = null;
            reply resp1;
        }

        worker bar {
            message resp2 = {};
            reply resp2;
        }
    } join (all) (message[] allReplies) {

    } timeout (30000) (message[] msgs) {
        return null, null;
    }
}