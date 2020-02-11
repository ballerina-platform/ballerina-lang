import ballerina/io;

type SampleError error<string, Foo>;

const INVALID_ID = "Invalid ID";
type InvalidIdError error<INVALID_ID, InvalidIdDetail>;

type Foo record {|
    string message?;
    error cause?;
    boolean fatal;
|};

type InvalidIdDetail record {|
    string message?;
    error cause?;
    string id;
|};

public function main() {
    [string, string] v1 = ["Sample String", "Sample String 2"];
    Foo v2 = {message: "A", fatal: false};
    error e1 = error("Generic Error", message = "Failed");
    SampleError e2 = error("Sample Error", message = "Fatal", fatal = true);
    InvalidIdError e3 = InvalidIdError(id = "33456");

    basicMatch(v1);
    basicMatch(v2);
    basicMatch(e1);
    basicMatch(e2);
    basicMatch(e3);
}

function basicMatch(any|error v) {
    match v {
        var [tVar1, tVar2] => {
            io:println("Matched a value with a tuple shape");
        }
        var {message, fatal} => {
            io:println("Matched a value with a record shape");
        }
        // If the variable 'v' contains an `error` values of shape that matches
        // `InvalidIdError` it will be matched to `InvalidIdError` indirect
        // error match pattern.
        var InvalidIdError(id = id) => {
            io:println("Matched `InvalidError` id=", id);
        }
        // If the variable `v` contains an `error` value, it will be matched
        // to this pattern and the reason string and the detail record will be
        // destructed within the pattern block.
        var error (reason, message = message) => {
            io:println("Matched an error value : ",
            io:sprintf("reason: %s, message: %s", reason, message));
        }

        // If a rest binding pattern is used, the error details that are not
        // matched will be recorded in a map.
        var error (reason, message = message, ...rest) => {
            io:println("Matched an error value : ",
            io:sprintf("reason: %s, rest detail: %s", reason, rest));
        }
    }
}
