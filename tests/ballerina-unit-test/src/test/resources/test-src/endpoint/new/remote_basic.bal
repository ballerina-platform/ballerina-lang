type TestEP object {
    remote function action1(string s, int i) returns boolean {
        if (i > 5) {
            return true;
        }
        return false;
    }

    remote function action2(string s, boolean b) returns int;

    function func1(string s) {

    }

    function func2(string s) returns int {
        return 5;
    }
};


remote function TestEP::action2(string s, boolean b) returns int {
    return 10;
}

function test1() returns boolean{
    TestEP x = new;
    x.func1("abc");
    boolean b = x->action1("test1", 4);
    return b;
}
