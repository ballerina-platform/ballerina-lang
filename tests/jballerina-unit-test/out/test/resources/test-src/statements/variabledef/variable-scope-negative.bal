

function testVariableIfScope () returns (int) {
    int a = 90;
    int b = 50;
    if (a > 20) {
        int c = 20;
    } else {
        int k = 60;
        if (b < 100) {
            k = b + 30;
        }
    }
    return k;
}

function testVariableElseScope() returns (int) {
    int a = 10;
    if(a > 20) {
        a = 50;
    } else {
        int b = 30;
    }

    return b;
}

function testVariableWhileScope() {
    int a = 0;
    while( a < 5) {
        a = a + 1;
        int b = a + 20;
    }
    int sum = b;
}


service myService = service {

    resource function myResource1(string s) {
        string res = "abc";
        int b = a + 50;
    }

    resource function myResource2(string s) {
        string res = "abc";
        int c = b + 50;
    }
};
