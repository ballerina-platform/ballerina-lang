function testNullInForkJoin () (string, string) {
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
    } join (all) (map allReplies) {
        any[] temp;
        temp,_ = (any[])allReplies["foo"];
        string m1;
        m1, _ = (string) temp[0];
        temp,_ = (any[])allReplies["bar"];
        string m2;
        m2, _ = (string) temp[0];
        return m1,m2;
    } timeout (30000) (map msgs) {

    }
}