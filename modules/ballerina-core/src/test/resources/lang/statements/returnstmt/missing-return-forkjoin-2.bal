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
    } join (all) (any[][] allReplies) {
        return (message)allReplies[0][0], (message)allReplies[1][0];
    } timeout (30000) (any[][] msgs) {

    }
}