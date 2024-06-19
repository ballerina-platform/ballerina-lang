client class MyClass {
    resource function get First(int val) returns int {

    }

    resource function get First/[string second]() returns int {

    }

    resource function get First/[string second]/Third(string val) returns int {

    }
}

MyClass myClass = new ();

function fn1() {
    int val = myClass->/;
}

function fn2() {
    int val = myClass->/First/
}
