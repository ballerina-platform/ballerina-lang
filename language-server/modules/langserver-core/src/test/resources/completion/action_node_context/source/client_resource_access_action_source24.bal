type PathParamType string|int|boolean|float;

client class MyClient {

    resource function get [PathParamType... path]() {

    }

    resource function post [PathParamType... path]() {

    }
}

public function test() {
    MyClient cl = new ();

    cl -> /
}
