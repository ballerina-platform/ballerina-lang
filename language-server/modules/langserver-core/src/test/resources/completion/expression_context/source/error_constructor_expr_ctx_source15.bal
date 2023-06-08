type ErrorData record {|
    int code;
    string detail;
|};

type ERR error<ErrorData>;

function name() {
    ERR testError = error ERR("message", "cause", );
}
