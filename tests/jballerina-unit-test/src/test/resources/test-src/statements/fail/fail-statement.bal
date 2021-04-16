type Error error<record {| |}>;

public function testFailStmt() returns error {
    Error err = error("Custom error thrown explicitly.");
    fail err;
}
