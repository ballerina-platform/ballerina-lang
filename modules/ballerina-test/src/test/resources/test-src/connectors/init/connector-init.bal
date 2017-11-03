package connectors.init;

connector Foo (string name, int age) {

    int[] gg = [4];
    int t = test (age) + 1;
    int y = 10 + t;
    int x = y + 20 + gg[0];
    string myName = name + "sameera";

    action getName (string name) (string, int) {
        return x + 90 + ":" + myName, x + 90;
    }

    action getName1 (string name, int age) (int) {
        return x + 80;
    }
}

function testConnectorInit (string name, int age) (int, string){
    Foo f = create Foo (name, age);
    return invokeConnector (f, "sam");
}

function invokeConnector (Foo myFoo, string name) (int, string) {
    endpoint<Foo> ep {
        myFoo;
    }
    int i;
    string str;
    str, i = ep.getName (name);
    return i, str;
}

function test (int x) (int y) {
    y = x * 2;
    return;
}

connector Bar(any name, json age) {
    action getNameAndAge(any name, json age) (any, json) {
        return name, age;
    }
}
 
function testConnectorInitWithImplicitCastableTypes() (string, int){
    endpoint<Bar> ep {

    }
    Bar bar;
    string arg1 = "John";
    int arg2 = 40;
    bar = create Bar(arg1, arg2);
    bind bar with ep;
    var a, j = ep.getNameAndAge(arg1, arg2);
    
    var s, _ = (string) a;
    var i, _ = (int) j;
    return s, i;
}
