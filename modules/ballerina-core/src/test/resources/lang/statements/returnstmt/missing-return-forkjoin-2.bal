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
    } join (all) (any[] allReplies) {
        return (message)allReplies[0], (message)allReplies[1];
    } timeout (30000) (any[] msgs) {

    }
}