import ballerina/module1;

function createFunctionInWorker() {
    worker A returns int {
        return foo();
    }

    worker B returns string {
        foo(32);
        return "bar";
    }

    worker C {
        return foo();
    }

    worker D returns module1:TestRecord2 {
        return foo();
    }

    fork {
        worker E returns int {
            return foo();
        }
    }
}
