function testForkJoinWithoutTimeoutExpression() returns (int, float)|error {
    int x;
    float y;
    fork {
        worker W1 {
            100 -> fork;
        }
        worker W2 {
            1.23 -> fork;
        }
    } join (all) (map<any> results) {
        x =check <int> results.W1;
        y =check <float> results.W2;
    }
    return (x, y);
}