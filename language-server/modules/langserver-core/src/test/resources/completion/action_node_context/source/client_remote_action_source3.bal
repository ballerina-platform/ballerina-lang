public client isolated class Client {
    remote isolated function get(string path, string name, int count = 10) returns string  {
        return "Hello from sample client";
    }
}

final Client clientEndpoint = new ();

public function main() {
    string resp = clientEndpoint->get();
}
