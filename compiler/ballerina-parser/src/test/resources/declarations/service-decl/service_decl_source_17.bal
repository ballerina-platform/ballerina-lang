// resource-path-segment cannot follow a resource-path-rest-param
service on listner1 {

    resource function get [string... x]/bar() {

    }

    resource function get [string... x]/[string s]() {

    }

    resource function get [string... x]/[string... x]() {

    }

    resource function get foo/[string... x]/bar() {

    }

    resource function get foo/[string... x]/[string s]() {

    }

    resource function get foo/[string... x]/[string... x]() {

    }
}
