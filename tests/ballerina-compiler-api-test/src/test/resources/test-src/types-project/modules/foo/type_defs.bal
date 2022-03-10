type ErrorDetail1 record {|
    string cause;
    int errorCode;
|};

type MyErr error<ErrorDetail1>;

type MyInt int|float;

public enum Language {
    SI = "Sinhalese",
    TA = "Tamil",
    EN = "English"
}

public function main() {
    MyErr myerr = error()
}
