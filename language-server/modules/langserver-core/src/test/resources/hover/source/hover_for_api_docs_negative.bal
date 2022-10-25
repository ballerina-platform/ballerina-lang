public client class Client {
    public string url;

    public function init(string url) {
        self.url = url;
    }

    resource function post path2/[string id]/path4(string str, int a) returns string {
        return "";
    }

}


public function test() {
    Client cl = new ("");
    string a  = cl-> /path2/[""]/path4.post()
}
