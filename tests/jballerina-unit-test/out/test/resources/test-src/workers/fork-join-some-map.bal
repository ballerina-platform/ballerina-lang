function testForkJoinReturnAnyType()(int, string) {
    return testForkJoinReturnAnyTypeVM();
}

function testForkJoinReturnAnyTypeVM()(int, string) {
    int p;
    string q;
    string r;
    float t;
    fork {
        worker W1 {
            println("Worker W1 started");
            int x = 23;
            string a = "aaaaa";
            x, a -> fork;
        }
        worker W2 {
            println("Worker W2 started");
            string s = "test";
            float u = 10.23;
            s, u -> fork;
        }
    } join (some 1) (map<any> results) {
        println("Within join block");
        //any[] t1;
        //t1,_ = (any[]) results["W1"];
        println("After any array cast");
        p = (int) results[0];
        //println("After int cast");
        //q = (string) results[1][0];
        //r = (string) results[0][1];
        //t = (float) results[1][1];
        println(p);
        //println(r);
        //println(q);
        //println(t);
    }

    println("After the fork-join statement");
    p = 111;
    q = "eeee";
    return p, q;
}
