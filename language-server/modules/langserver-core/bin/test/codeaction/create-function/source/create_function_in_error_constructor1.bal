type ErrorDetail record {|
    int code;
|};

type Error1 error<ErrorDetail>;

public function main() {
    var err1 = error Error1(getMessage());
}
