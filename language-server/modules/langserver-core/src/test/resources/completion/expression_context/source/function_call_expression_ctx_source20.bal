type Config record {
    string host;
    int port;
};

function foo(*Config c) {
    int a = 10;
}
public function main() returns error? {
   foo();
}
