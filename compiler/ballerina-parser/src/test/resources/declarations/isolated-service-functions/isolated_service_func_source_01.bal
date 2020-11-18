service on ep {

    isolated resource function foo1(string b) {
    }

    resource isolated function foo2(int i) returns error? {
    }

    isolated function foo3() returns boolean {
    }

    transactional isolated resource function foo4(Bar b) {
    }

    isolated transactional resource function foo5() {
    }

    resource isolated transactional function foo6() {
    }

    isolated transactional function foo7() {
    }

    isolated function foo8() {
    }
}
