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
        w1 =? <any[]> results["W1"];
        w2 =? <any[]> results["W2"];
        x =? <int> w1[0];
        y =? <float> w2[0];
    }
    return (x, y);
}