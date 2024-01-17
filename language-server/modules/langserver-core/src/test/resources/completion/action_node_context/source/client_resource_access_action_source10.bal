client class MyClient {

    remote function remote1() {

    }

    resource function get path1() returns string {
        return "";
    }

    resource function post .(string a, int b) {
    }

    resource function get [string... ids]() {

    }

    resource function get path1/[string id1]() {

    }

    resource function get path1/[string id1]/path2() returns int {
        return 0;
    }

    resource function post path1/[string id1]/path2/[string... ids](string b, string... ids2) {

    }
    
    resource function post path1/[string id1]/path3(string b, string... ids2) {
    
    }
}

public function test() {
    MyClient cl = new ();
    string s = "path";

    cl -> /path1/["param"]/pat

}
