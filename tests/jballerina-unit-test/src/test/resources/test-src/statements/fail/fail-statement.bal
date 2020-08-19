
public function testFailStmt() returns error {

    error err = error("Custom error thrown explicitly.");
    fail err;
}
