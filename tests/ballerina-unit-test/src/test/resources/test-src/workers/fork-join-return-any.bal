
function testForkJoinReturnAnyType() returns (int, string) {
    int p;
    string q;
    string r;
    float t;
    fork {
        worker W1 {
            int x = 23;
            string a = "aaaaa";
            (x, a) -> fork;
        }
        worker W2 {
            string s = "test";
            float u = 10.23;
            (s, u) -> fork;
        }
    } join (all) (map results) {
        any t1 = <any> results["W1"];
        (p, q) = check <(int,string)> t1;
        any t2 = <any> results["W2"];
        (r, t) = check <(string,float)> t2;
    }
    return (p, q);
}
