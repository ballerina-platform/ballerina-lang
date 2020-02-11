import ballerina/io;

type SampleErrorData record {
    string message?;
    error cause?;
    string info;
    boolean fatal;
};

type SampleError error<string, SampleErrorData>;

public function main() {
    string reason;
    string info;
    boolean fatal;
    // This error-destructure binding pattern will destructure an `error` value of the type `SampleError`
    // and assign the values to the relevant variable references.
    // The value of the reason string in the `SampleError` will be assigned to the variable `reason`.
    // The values in the detail mapping will be assigned to the relevant variables.
    error(reason, info = info, fatal = fatal) = getSampleError();
    io:println("Reason String: " + reason);
    io:println("Info: ", info);
    io:println("Fatal: ", fatal);

    string reasonTwo;
    map<anydata|error> params;
    // The detail mapping can be destructured into an existing `map<anydata|error>`-typed variable by using a rest parameter.
    error(reasonTwo, ...params) = getSampleError();
    io:println("Reason String: ", reasonTwo);
    io:println("Detail Map: ", params);
    
    // The underscore '_' sign can be used to ignore either the reason string or the detail mapping.
    string? detailMsg;
    error(_, detailMsg = detailMsg) = getRecordConstrainedError();
    io:println("Detail Message: ", detailMsg);
}

function getSampleError() returns SampleError {
    SampleError e = error("Sample Error", info = "Detail Info", fatal = true);
    return e;
}

type Foo record {|
    string message?;
    error cause?;
    string detailMsg;
    boolean isFatal;
|};

function getRecordConstrainedError() returns error<string, Foo> {
    error<string, Foo> e = error("Some Error", detailMsg = "Failed Message", isFatal = true);
    return e;
}
