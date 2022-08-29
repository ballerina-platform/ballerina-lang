int client = 1;
// client:x a = 2; // https://github.com/ballerina-platform/ballerina-lang/issues/37461

function fn() {
    string client;
    any b = client:y;
}
