type ErrorData record {|
    string message?;
    error cause?;
    int errID?;
|};

type ERR distinct error<ErrorData>;

function name() {
    ERR ce = error ERR("", message = "", er);
}
