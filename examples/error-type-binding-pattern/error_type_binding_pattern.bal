import ballerina/io;

type SampleError error<string, map<anydata|error>>;

public function main() {
    // This error type binding pattern will de-structure an `error` of type `SampleError` and create two variables as follows:
    // The value of the `reason string` in `SampleError` will be set to a new `string` variable `reason`.
    // The value of the `detail mapping` will be set to a new `map<anydata|error>` variable `detail`.
    SampleError error(reason, detail) = getSampleError();
    io:println("Reason String: " + reason);
    io:println("Detail Mapping: " + io:sprintf("%s", detail));

    // The `detail mapping value` can further be de-structured using a record binding pattern.
    SampleError error(reasonTwo, { detail: detailTwo, fatal }) = getSampleError();
    io:println("Reason String: " + reasonTwo);
    io:println("Detail Mapping Field One: " + io:sprintf("%s", detailTwo));
    io:println("Detail Mapping Field Two: " + io:sprintf("%s", fatal));

    // Error type binding patterns can be used with `var` to infer the type from the right hand side.
    // Since the types of the new variables are based on the type of the type binding pattern, using `var` will
    // infer the types from the right hand side.
    var error(vReason, vDetail) = getSampleError();
    // Type of `vReason` is inferred as `string`.
    io:println("Reason String: " + vReason);
    // Type of `vDetail` is inferred as `map<anydata|error>`.
    io:println("Detail Mapping: " + io:sprintf("%s", vDetail));

    // Underscore '_' can be used to ignore either the `reason string` or the `detail mapping`.
    error<string, Foo> error(_, fooRec) = getRecordConstrainedError();
    io:println("Detail Mapping: " + io:sprintf("%s", <Foo> fooRec));
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
