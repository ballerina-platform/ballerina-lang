function testNestedScopes() returns (int) {
    int a = 2;

    scope scopeA {
        scope scopeB {
            string s = "abc";
            a = 5;
        } compensation(a) {
            //compensate
        }
    } compensation() {

    }
    a = 3;
    compensate scopeA;
    return a;
}

function testLoopScopes() returns (int) {
    int a = 2;

    int k = 1;
    while (k < 5) {
        scope scopeA {
            scope scopeB {
                a = 5;
            } compensation() {
                string abc = "abc";
            }
        } compensation(a) {

        }
        k = k +2;
    }
    a = 3;
    compensate scopeA;
    return a;
}

