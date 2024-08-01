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
