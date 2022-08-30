client "http://www.example.com/apis/one.yaml" as foo;

public function main() {
    client "http://www.example.com/apis/one.yaml" as bar;
    foo:client x;
    bar:client y;
}
