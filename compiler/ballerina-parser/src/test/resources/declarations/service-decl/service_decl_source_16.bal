service on listner1 {
    resource function get [string x]() {

    }

    resource function get foo/[string x]() {

    }

    resource function get foo/[string... x]() {

    }

    resource function get foo/baz/[string s]/bar() {

    }

    resource function get foo/[string s]/[string s]/bar() {

    }

    resource function get foo/[string i]/bar/[string... x]() {

    }

    resource function get foo/[string s]/bar/[string s]/baz() {

    }

    resource function get foo/[@untainted string i]/bar/[@untainted string... x]() {

    }

    resource function get person/[Foo]() {

    }

    resource function get [Foo]() {

    }

    resource function get [string s]/[Foo]/name() {

    }

    resource function get [string s]/[Foo...]() {

    }
}
