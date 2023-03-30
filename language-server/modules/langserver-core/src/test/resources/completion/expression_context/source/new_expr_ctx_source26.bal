type MyType string;

function testNew() returns error? {
    string myStr = "";
    int myInt = 0;
    MyClass cls = check new ();
}

client class MyClass {
    function init(string a, int b) returns error? {

    }
}
