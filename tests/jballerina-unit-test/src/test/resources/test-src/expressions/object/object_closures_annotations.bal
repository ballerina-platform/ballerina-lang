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

        @HSCfm {
                    hostRecField : hosty
                }
                public function getSum() returns int {
                    return 1 + 3;
                }
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

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
