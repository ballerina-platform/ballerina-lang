public type TestReason string;

type TestDetail record {|
    string message?;
    error cause?;
|};

public type ErrorTypeDesc1 error<TestReason, TestDetail>;

public function helloFunction() {
    var testVar = E
}
