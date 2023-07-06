type Rec1 record {
    int rec1Field1;
    int rec1Field2;
    int rec1Field3?;
};

type Rec2 record {
    int rec2Field1;
    int rec2Field2;
    int rec2Field3;
};

type Rec3 record {
    int rec3Field1;
    int rec3Field2;
};

type RecUnion Rec1|Rec2|record {int anonField1; string anonField2;};

type RecUnion2 Rec1|Rec2;

public function main() {
    RecUnion2 x = {
        rec1Field1: 0,
        rec1Field2: 0,
        
    }    
}
