
function testForkJoinReturnAnyType()(int, string) {
    int p;
    string q;
    string r;
    float t;
    fork {
        worker W1 {
            int x = 23;
            string a = "aaaaa";
            x, a -> fork;
        }
        worker W2 {
            string s = "test";
            float u = 10.23;
            s, u -> fork;
        }
    } join (all) (map results) {
        any[] t1;
        t1,_ = (any[]) results["W1"];
        p, _ = (int) t1[0];
        q, _ = (string) t1[1];
        t1,_ = (any[]) results["W2"];
        r, _ = (string) t1[0];
        t, _ = (float) t1[1];
    }
    p = 111;
    q = "eeee";
    return p, q;
}
