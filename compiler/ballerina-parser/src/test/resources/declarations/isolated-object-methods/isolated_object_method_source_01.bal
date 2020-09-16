client class Foo {

    isolated remote function foo1(string b) {
    }

    remote isolated function foo2(int i) returns error? {
    }

    isolated function foo3() returns boolean {
    }

    transactional isolated remote function foo4(Bar b) {
    }

    isolated transactional remote function foo5() {
    }

    remote isolated transactional function foo6() {
    }

    isolated transactional function foo7() {
    }

    isolated function foo8() {
    }
}
