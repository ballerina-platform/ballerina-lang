client class MyClient {
    resource function accessor ["A31" a]() {

    }

    resource function put ["A31"|"A32" a]() {

    }

    resource function post ["A32" a]() {

    }
}

public function main() {
    var cl = new MyClient();
    cl ->/["A31"].
}
