
function testForkReturnAnyType() returns [int, string]|error {
    int p = 0;
    string q = "";
    string r;
    float t;

    fork {
        worker W1 returns [int, string] {
            int x = 23;
            string a = "aaaaa";
            return [x, a];
        }
        worker W2 returns [string, float] {
            string s = "test";
            float u = 10.23;
            return [s, u];
        }
    }
    map<any> results = wait {W1, W2};
    any t1 = results["W1"];
    if t1 is [int,string] {
        [p, q] = t1;
    }
    any t2 = results["W2"];
    if t2 is [string,float] {
        [r, t] = t2;
    }
    return [p, q];
}
