import ballerina/http;

public function main() returns error? {
    http:Client cl= check new("");
    cl->/"path1"/path2/.post();
}
