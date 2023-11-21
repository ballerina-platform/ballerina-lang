client class MyClient {
    resource function get first/[string second]/third() {}
}

public function main() {
    string someVar = "check";
    MyClient cl = new;
    cl->/first/[someVar]/;
}
