
public function testFailExpr() returns error {

    error err = error("Custom error thrown explicitly.");
    error outputErr = fail err;
}
