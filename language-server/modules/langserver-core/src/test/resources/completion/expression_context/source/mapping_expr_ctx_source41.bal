type Rec3 record {
    int rec3Field1;
    int rec3Field2;
};

type RecUnion Rec1|Rec2|record {int anonField1; string anonField2;};

type RecUnion2 Rec1|Rec2;

public function main() {
    int rec3Field2 = 12;
    Rec3 rec3 = {
        rec3Field1: 0,
        r
    }   
}
