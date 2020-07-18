
public function testFailAction() returns error {

    error|int err = error("Custom error thrown explicitly.");
    fail err;
}
