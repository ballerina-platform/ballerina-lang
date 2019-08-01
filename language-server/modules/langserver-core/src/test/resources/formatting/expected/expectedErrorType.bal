function func1() {
    error<string, map<anydata>> error1 = error("Error One", detail = "failed");
    error<string, map<anydata>> error2 = error("Error One",
    detail = "failed");

    error<string> error3 = error("Error One");

    error
    <
    string
    ,
    map<anydata>
    >
    error4
    =
    error("Error One",
    detail = "failed"
    );
}
