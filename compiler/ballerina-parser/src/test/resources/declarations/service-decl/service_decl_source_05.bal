service foo on listner {

    // Test missing resource qualifier
    function get bar/baz() {
        int a = 10;
    }

    // Test missing resource path
    resource function get() {
        int b = 11;
    }

    // Test with invalid qualifiers
    remote resource function get name () {

    }
    resource remote function get name () {

    }

    // Test missing accessor name and resource path
    resource function () {

    }
}
