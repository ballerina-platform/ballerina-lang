function testNullInForkJoin () (message, message) {
    message m = null;
    fork {
        worker foo {
            message resp1 = null;
            resp1 -> ;
        }

        worker bar {
            message resp2 = {};
            resp2 -> ;
        }
    } join (all) (any[][] allReplies) {

    } timeout (30000) (any[][] msgs) {
        return null, null;
    }
}