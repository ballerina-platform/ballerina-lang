import ballerina/io;

public function main(string... args) {
    testTypes();
}

function testTypes() {
    int a = 1;
    io:println(a);

    float b = 1;
    io:println(b);

    decimal c = 10;
    io:println(c);

    string d = "";
    io:println(d);

    boolean e = false;
    io:println(e);

    () f = ();
    io:println(f);

    //xml g = xml `<a></a>`; //xml-xpr not supported yet
    //io:println(g);

    json h = {foo : 1};
    json h2 = null;
    io:println(h);
    io:println(h2);

    handle? i = ();
    io:println(i);
}
