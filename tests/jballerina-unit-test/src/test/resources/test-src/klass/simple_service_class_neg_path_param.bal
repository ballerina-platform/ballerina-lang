
service class SClass {
    string message  = "";

    remote function foo(int i) returns int {
        return i + 100;
    }

    resource function get barPath() returns string {
        return self.message;
    }

    resource function get foo/path() returns string {
        return self.message + "foo";
    }

    resource function get .() returns string {
        return self.message + "dot";
    }

    resource function get foo/baz(string s) returns string {
        return s;
    }

    resource function get foo/[int i]() returns int {
        return i;
    }

    resource function get foo/[string s]/[string... r]() returns string {
        string result = s + ", ";
        foreach string rdash in r {
            result += rdash;
        }
        return result;
    }

    resource function get foo/[json j]/[anydata a]/[boolean b]/[anydata... r]() returns string {
        return "";
    }

}
