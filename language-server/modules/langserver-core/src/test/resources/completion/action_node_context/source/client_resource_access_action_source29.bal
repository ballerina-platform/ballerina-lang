client class MyClient {
    resource function get [PathParamType... path]() {

    }

    resource function post [PathParamType... path]() {

    }
}

public type PathParamType boolean|int|float|decimal|string;

public function main(string audioUrl, string toLanguage) returns error? {
    MyClient cl = new;
    cl -> /path/path2/;
}
