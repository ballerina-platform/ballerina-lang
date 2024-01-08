client class MyClient {
    resource function accessor [string:Char a]() {

    }

    resource function put [string:Char|int a]() {

    }

    resource function post [int a]() {

    }
}

public function main() {
    var cl = new MyClient();
    cl ->/A.
}
