client class MyClient {

    remote function remote1(string id, string purse) {

    }

    resource function get users() returns string {
        return "";
    }

    resource function get [string... ids]() {

    }

    resource function post users/[int id1]/alias(string b, string... ids2) {

    }
}

public function test() {
    MyClient cl = new ();

    cl->/users/

    myFunction();
}

function myFunction() {

}
