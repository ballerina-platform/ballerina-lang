type ErrorData record {|
    int code;
    string detail;
|};

type ERR error<ErrorData>;

function name(int z, error newErr) {
    ERR testError = error ERR("message", "cause", code = );
}
