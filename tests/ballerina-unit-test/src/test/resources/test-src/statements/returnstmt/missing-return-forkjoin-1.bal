function testNullInForkJoin () returns (string, string) {
    string m = "";
    fork {
        worker foo {
            string resp1 = "";
            resp1 -> fork;
        }

        worker bar {
            string resp2 = "";
            resp2 -> fork;
        }
    } join (all) (map<any> allReplies) {

    } timeout (30000) (map<any> msgs) {
        return ("", "");
    }
}
