function testNullInForkJoin () (message, message) {
    message m = null;
    fork (m) {
        worker foo (message m) {
            message resp1 = null;
            reply resp1;
        }

        worker bar (message m) {
            message resp2 = {};
            reply resp2;
        }
    } join (all) (message[] allReplies) {

    } timeout (30000) (message[] msgs) {
        return null, null;
    }
}