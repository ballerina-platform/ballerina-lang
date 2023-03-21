client class MyClient {

    remote function remote1(string id) {

    }

    resource function get users() returns string {
        return "";
    }

    resource function post .(string a, int b) {
    }

    resource function get [string... ids]() {

    }

    resource function get name/[string id1]() {

    }

    resource function get users/[string id1]/name() returns int {
        return 0;
    }

    resource function post users/[string id1]/names/[string... ids](string b, string... ids2) {

    }

    resource function post users/[string id1]/alias(string b, string... ids2) {

    }
}

public function test() {
    MyClient cl = new ();
    cl -> /users/abc/alias.post()
}
