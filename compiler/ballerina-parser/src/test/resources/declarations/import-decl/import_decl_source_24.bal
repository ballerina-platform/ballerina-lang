// Override lang.int with custom import
import ballerina/foo.bar.baz as int;

public function foo() {
    // Using module prefix with predefined prefix
    int a = int:sum(1, 2, 3);
}
