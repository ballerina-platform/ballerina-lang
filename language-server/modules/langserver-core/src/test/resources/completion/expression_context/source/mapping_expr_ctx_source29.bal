type Application record {|
    string id;
    string name;
    int 'version;
|};

type ErrorData record {
    string errorCode;
    Application application;
};

type CustomError error<ErrorData>;

public function checkError() returns CustomError? {
    return error CustomError("Custom error", errorCode = "123", application = {});
}
