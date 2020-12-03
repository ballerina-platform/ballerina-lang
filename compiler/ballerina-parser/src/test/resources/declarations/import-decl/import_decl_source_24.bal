// Override lang.int with custom import
import ballerina/foo.bar.baz as int;

// Predeclared prefix in annotation and listener decl
@int:annot
listener stream:Element myListner = getListner();

public function foo() {
    // Using module prefix with predeclared prefix
    int a = int:sum(1, 2, 3);
    // Predeclared prefix in new expression
    Person p = new int:getInstance();
}
