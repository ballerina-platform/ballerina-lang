type Info record {|
    int id;
    string name?;
    anydata description;
|};

type AnnotationConfig record {|
    Info info;
    int|record {*Info; string description;} doc?
|};

const annotation AnnotationConfig CustomAnnotation on source type, source const, source class,
                                                        source function, source object function,
                                                        source service, source return, source parameter,
                                                        source field, source listener, source var,
                                                        source annotation, source worker;

@CustomAnnotation {
    info: {},
    doc: {}
}
type TestString string;

@CustomAnnotation {
    info: {},
    doc: {}
}
enum TestEnum {
    FIRST, SECOND
}

@CustomAnnotation {
    info: {},
    doc: {}
}
const int TEST_CONST = 32;

@CustomAnnotation {
    info: {},
    doc: {}
}
function testFunction() {
}

@CustomAnnotation {
    info: {},
    doc: {}
}
isolated class TestClass {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    int i;

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    function testClassFunction() {
    }

    public function init() {
    }

    public function attach(service object {} s, string[]|string|() name = ()) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

@CustomAnnotation {
    info: {},
    doc: {}
}
type TestObject object {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    int i;

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    function testObjectFunction();
};

@CustomAnnotation {
    info: {},
    doc: {}
}
service / on new TestClass() {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    remote function name() {
    }

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    resource function accessor path() {
    }
}

@CustomAnnotation {
    info: {},
    doc: {}
}
type A service object {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    remote function name();

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    resource function accessor path();
};

var a = service object {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    int i;

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    remote function name() {
    }

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    resource function accessor path() {
    }
};

function testFunctionWithReturn() returns @CustomAnnotation {info: {}, doc: {}} int => 10;

function testFunctionWithParams(@CustomAnnotation {info: {}, doc: {}} int i,
        @CustomAnnotation {info: {}, doc: {}} int j = 32,
        @CustomAnnotation {info: {}, doc: {}} *Info info,
        @CustomAnnotation {info: {}, doc: {}} anydata... rest) {
}

type TestRecord record {|
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    string value;
|};

@CustomAnnotation {
    info: {},
    doc: {}
}
listener TestClass testListener = new;

@CustomAnnotation {
    info: {},
    doc: {}
}
var testModuleVarDecl = 32;

function testLocalVarDecl() {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    var testLocalVarDecl = 32;

    _ = let @CustomAnnotation {info: {}, doc: {}}
        var testLetVarDecl = 32
        in testLetVarDecl + 32;
}

@CustomAnnotation {
    info: {},
    doc: {}
}
annotation TestAnnotation on field;

function testWorker() {
    _ = @CustomAnnotation {info: {}, doc: {}} start testFunction();

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    worker TestNamedWorker {
    }
}

@CustomAnnotation {
    info: {},
    doc: {}
}
client class TestClient {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    int i;

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    remote function name() {
    }

    @CustomAnnotation {
        info: {},
        doc: {}
    }
    resource function accessor path() {
    }
}

@CustomAnnotation {
    info: {},
    doc: {}
}
type TestError error<map<anydata>>;

function testErrorVariable() {
    @CustomAnnotation {
        info: {},
        doc: {}
    }
    error testErrorVar = error("Test Error");
}

[@CustomAnnotation {info: {}, doc: {}} int, int] [first, second] = [1, 2];

// var fnPointer = function() {};

// function testFunctionPointer() {
//     @CustomAnnotation {
//         info: {},
//         doc: {}
//     }
//     fnPointer();
// }

client class TestResourceSegments {
    resource function testResourceVariable path/[@CustomAnnotation {info: {}, doc: {}} string i]() {
    }

    resource function testRestVariable path/[@CustomAnnotation {info: {}, doc: {}} string ... i]() {
    }
}

@CustomAnnotation {
    info: {},
    doc: {}
}
function testExternalFunction() = external;

int testTypeCast = <@CustomAnnotation {info: {}, doc: {}} int> 12.5;
