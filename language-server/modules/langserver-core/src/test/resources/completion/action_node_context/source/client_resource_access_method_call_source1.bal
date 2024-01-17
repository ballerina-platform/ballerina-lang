client class MyClient {

    resource function get user/[int id]() returns json {
        return {};
    }

    resource function post user/[int id](string referrer) {

    }

    resource function get user/[int id]/[string... params]() {
    }

    resource function post user/[string... params](string id, string age, string name) returns string {
            return "";
    }

}

public function testClient(string param1, int count) returns error? {
    MyClient myClient = new ();
    string response = myClient -> /user/path1/path2.post(,12, name = "J");
}
