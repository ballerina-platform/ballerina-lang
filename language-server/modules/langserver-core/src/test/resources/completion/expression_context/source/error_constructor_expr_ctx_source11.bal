type ErrorDesc record {|
    int code;
    string stack;
|}

type Error1 error<ErrorDesc>;

const string message = "Error message"

public function func1(string param1) {
    string msg = "Error message";
    Error1 err1 = error Error1(, code = 10)
}

function getMessage() returns string {
    return "Error message";
}
