import ballerina/io;

type SampleError error<string, map<anydata|error>>;

public function main() {
    string reason;
    map<anydata|error> detail;
    // This error destructure binding pattern will destructure an `error` value of type `SampleError`
    // and assign the values to two variable references as follows:
    // The value of the `reason string` in `SampleError` will be assigned to the variable `reason`.
    // The value of the `detail mapping` will be assigned to the variable `detail`.
    error(reason, detail) = getSampleError();
    io:println("Reason String: " + reason);
    io:println("Detail Mapping: " + io:sprintf("%s", detail));

    string reasonTwo;
    anydata|error detailTwo;
    anydata|error fatal;

    // The `detail mapping` can further be de-structured into existing variable references.
    error(reasonTwo, { detail: detailTwo, fatal }) = getSampleError();
    io:println("Reason String: " + reasonTwo);
    io:println("Detail Mapping Field One: " + io:sprintf("%s", detailTwo));
    io:println("Detail Mapping Field Two: " + io:sprintf("%s", fatal));

    // Underscore '_' can be used to ignore either the `reason string` or the `detail mapping`.
    Foo fooRec;
    error(_, fooRec) = getRecordConstrainedError();
    io:println("Detail Mapping: " + io:sprintf("%s", fooRec));
}

function getSampleError() returns SampleError {
    SampleError e = error("Sample Error", { detail: "Detail Msg", fatal: true });
    return e;
}

type Foo record {|
    string detailMsg;
    boolean isFatal;
|};

function getRecordConstrainedError() returns error<string, Foo> {
    error<string, Foo> e = error("Some Error", { detailMsg: "Failed Message", isFatal: true });
    return e;
}
