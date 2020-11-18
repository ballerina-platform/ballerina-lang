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

    resource function get foo/[string… i]/bar() {

    }

    resource function get foo/[string s]/bar/[string s]/baz() {

    }
}
