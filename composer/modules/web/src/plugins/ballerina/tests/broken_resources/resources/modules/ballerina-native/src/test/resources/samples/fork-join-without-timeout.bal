import ballerina/lang.system;

function testForkJoinWithoutTimeoutExpression()(int, float) {
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
    w1,_ = (any[]) results["W1"];
    w2,_ = (any[]) results["W2"];
    x, _ = (int) w1[0];
    y, _ = (float) w2[0];
    system:println(x);
    system:println(y);
    }
    system:println(x);
    system:println("After the fork-join statement ");
    return x, y;
}