type Summary record {|
    int total;
    int rejected;
    string reference;
|};

type Data record {|
    string id;
    string value;
    int count;
    Summary summary;
|};

public function main() {
    Data data = createData();
}
