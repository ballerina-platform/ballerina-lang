type PathParamType string|int|boolean|float;

client class MyClient {

    resource function post path1/[PathParamType... path]/find() {

    }

}

public function test() {
    MyClient cl = new ();

    cl -> /
}
