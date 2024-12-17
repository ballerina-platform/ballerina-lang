client class MyClass {
    resource function get First() {

    }

    resource function get First/[string second]() {

    }

    resource function get First/[string second]/Third() {

    }
}

MyClass myClass = new ();

function fn1() {
    myClass->/;
}

function fn2() {
    myClass->/First/
}
