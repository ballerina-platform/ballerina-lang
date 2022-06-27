public function testTransactionCall() {
    var successClient = client object {
        transactional resource function get path/[string]() returns int {
            return 1;
        }
        resource function get path2/[string]() {
            testFunction();
        }
    };
    int result = successClient->/path/["a"]; // No Error Found. Issue - #36736.
}

public transactional function testFunction() {

}
