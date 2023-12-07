import test_project.mod;

type Rec record {
    string name;
};

public function main() {
    map<string>? _ = {[mod:NAME]: "Amy"};
    map<string>? _ = {[mod:NAME]: mod:NAME};
    Rec? _ = {[mod:NAME]: mod:NAME};
}
