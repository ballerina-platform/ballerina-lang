type Internal record {
    int fField1;
    int fField2;
};

type Outer record {
    string name;
    Internal[] functions;
};

public function main() {
    Outer[] c = [{
        functions: [{
            
        }]
    }]
}
