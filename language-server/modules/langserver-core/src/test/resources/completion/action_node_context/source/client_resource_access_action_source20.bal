client class MyClient {

    resource function get user/[int id]() returns json {
        return {};
    }

    resource function post user/[int id](string referrer) {

    }

    resource function get user/[int id]/[string... params]() {
    }

    resource function post user/[int id]/[string... params]() {
    }

    resource function get [string... params](string id) returns string {
        return "";
    }

    resource function post [string... params]() {

    }

    resource function post user/[string... params](string id, string age, string name) {

    }

    resource function put [string... params]() {

    }

    resource function patch [int... params]() {

    }
}

public function testClient(string param1, int count) returns error? {
    MyClient myClient = new ();
    string response = my->/user/[1]/path1/path2/["path3"];
}

function getClient() returns MyClient {
    return new MyClient();
}
