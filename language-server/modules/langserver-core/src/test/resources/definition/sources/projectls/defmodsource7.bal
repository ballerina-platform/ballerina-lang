import projectls.lsmod1;

public client class MyClient {
    resource function get .() returns int {
        return 3;
    }

    resource function post .() returns int {
        return 2;
    }

    resource function get books/names() returns string[2] {
        return ["book1", "book2"];
    }
}

function foo() {
    MyClient myClient1 = new;
    _ = myClient1->/.get;
    _ = myClient1->/books/names.get;
    _ = myClient1->/.post;

    lsmod1:MyClient myClient2 = new;
    _ = myClient2->/.get;
    _ = myClient2->/books/names.get;
    _ = myClient2->/.post;
}
