class NonIsolatedClass {
    function init() {

    }

    function fn() {

    }

    isolated function fn2(int i) {
        
    }
}

isolated class IsolatedClass {
    function init(int i) {

    }

    function fn(int i) {

    }

    isolated function fn2(int i) {
        
    }
}

readonly class ReadonlyClass {
    isolated function fn(int i) {

    }
}
