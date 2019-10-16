import ballerina/io;

type SampleErrorData record {
    string message?;
    error cause?;
    string info;
    boolean fatal;
};

type SampleError error<string, SampleErrorData>;

public function main() {
    // This error-type binding pattern will destructure an `error` of the type `SampleError`, and create two variables as follows:
    // The value of the reason string in the`SampleError` will be set to a new `string` variable `reason`.
    // The values in the detail mapping will be set to new variables `info` and `fatal`.
    var error(reason, info = info, fatal = fatal) = getSampleError();
    io:println("Reason String: " + reason);
    io:println("Info: ", info);
    io:println("Fatal: ", fatal);

    // The detail mapping can also be destructured using a rest parameter.
    // `params` will be of the type `map<anydata|error>`, and will have the
    // `info` and `fatal` fields.
    var error(reasonTwo, ...params) = getSampleError();
    io:println("Reason String: ", reasonTwo);
    io:println("Detail Mapping: ", params);

    // When some error detail field names are provided rest parameter will only contain detail field that are not matched.
    var error(reasonThree, info=info2, ...filteredParams) = getSampleError();
    io:println("Detail Mapping: ", filteredParams);
    
    // The underscore '_' sign can be used to ignore either the reason string or the detail mapping.
    var error(_, detailMsg = detailMsg, isFatal = isFatal) = getRecordConstrainedError();
    io:println("Detail Message: ", detailMsg);
}

function getSampleError() returns SampleError {
    SampleError e = error("Sample Error", info = "Detail Msg", fatal = true);
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
