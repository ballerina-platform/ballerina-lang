function testForkJoinWithoutTimeoutExpression() returns (int, float) {
    int x;
    float y;
    fork {
        worker W1 {
            100 -> fork;
        }
        worker W2 {
            1.23 -> fork;
        }
    } join (all) (map results) {
        any[] w1;
        any[] w2;
        w1 =check <any[]> results["W1"];
        w2 =check <any[]> results["W2"];
        x =check <int> w1[0];
        y =check <float> w2[0];
    }
    return (x, y);
}