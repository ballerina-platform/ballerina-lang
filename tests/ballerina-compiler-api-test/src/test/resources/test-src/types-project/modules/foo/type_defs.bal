type ErrorDetail1 record {|
    string cause;
    int errorCode;
|};

type MyErr error<ErrorDetail1>;

type MyInt int|float;

public function main() {
    MyErr myerr = error()
}
