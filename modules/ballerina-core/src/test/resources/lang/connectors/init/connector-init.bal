package lang.connectors.init;

connector Foo (string name, int age) {

    int[] gg = [4];
    int t = test (age) + 1;
    int y = 10 + t;
    int x = y + 20 + gg[0];
    string myName = name + "sameera";

    action getName (Foo f, string name) (string, int) {
        return x + 90 + ":" + myName, x + 90;
    }

    action getName (Foo f, string name, int age) (int) {
        return x + 80;
    }
}


function testConnectorInit (string name, int age) (int, string){
    Foo f = create Foo (name, age);
    return invokeConnector (f, "sam");
}

function invokeConnector (Foo myFoo, string name) (int, string) {
    int i;
    string str;
    str, i = Foo.getName (myFoo, name);
    return i, str;
}

function test (int x) (int y) {
    y = x * 2;
    return;
}
