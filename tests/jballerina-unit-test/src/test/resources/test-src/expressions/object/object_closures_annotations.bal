public type HSC record {|
    string hostRecField = "default_host_name";
    boolean boolRecField = true;
|};

public annotation HSC HSCfa on field;
public annotation HSC HSCsa on service;

function createService(string hosty, decimal maxAgeMy, boolean allowCredentials) returns service object {
    string xField;
} {

    var httpService =
    @HSCsa {
        hostRecField : hosty
    }
    isolated service object {

        @HSCfa {
            // hostRecField : hosty
        }
        final string xField = hosty;
    };

    return httpService;
}

public function testAnnotations() {
    var obj = createService("hostKRv", 200, true);
    assertValueEquality("hostKRv", obj.xField);

    typedesc<object {}> t = typeof obj;
    HSC annotationVal = <HSC>t.@HSCsa;

    assertValueEquality("hostKRv", annotationVal.hostRecField);

    obj = createService("hostKRv boom", 200, true);
    typedesc<object {}> t2 = typeof obj;
    HSC annotationVal2 = <HSC>t2.@HSCsa;
    assertValueEquality("hostKRv boom", annotationVal2.hostRecField);
}

public type ObjectData record {|
    string descriptor = "";
|};

public annotation ObjectData OBJAnnots on class;

function testObjectConstructorAnnotationAttachment() {
    final string constructed = "ConstructedObject";

    var obj = @OBJAnnots {
        descriptor: constructed
        }
    object {
        int n = 0;
        string state = constructed;
        function inc() {
            self.n += 1;
        }
    };
    typedesc<object {}> t = typeof obj;
    ObjectData annotationVal = <ObjectData>t.@OBJAnnots;
    assertValueEquality("ConstructedObject", annotationVal.descriptor);
    assertValueEquality("ConstructedObject", obj.state);
}

type Record record {|
    int[] x;
|};

annotation Record A on service;
annotation B on service;
annotation C on service;

function fn(int[] arr) returns object {} {
    return
    @A {
        x: arr
    }
    @B
    service object {

    };
}

function testAnnotsOfServiceObjectConstructorInReturnStmt() {
    int[] arr = [1, 2, 3];

    object {} ob = fn(arr);
    typedesc<object {}> td = typeof ob;

    Record? a = td.@A;
    assertTrue(a !is ());
    int[] annotArr = (<Record> a).x;
    assertTrue(annotArr == arr);
    assertValueEquality(annotArr, [1, 2, 3]);
    arr.push(4);
    assertValueEquality(arr, [1, 2, 3, 4]);
    assertValueEquality(annotArr, [1, 2, 3]);

    true? b = td.@B;
    assertTrue(b);

    true? c = td.@C;
    assertTrue(c is ());
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(anydata actual) {
    assertValueEquality(actual, true);
}

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
