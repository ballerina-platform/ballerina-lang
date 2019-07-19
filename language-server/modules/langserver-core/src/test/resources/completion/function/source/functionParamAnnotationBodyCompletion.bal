type Annot1 record {
    string val1;
};

type Annot2 record {
    string val2;
};

type Annot3 record {
    string val3;
};

type Annot4 record {
    string val4;
};

annotation Annot1 v1 on parameter;
annotation Annot2 v2 on parameter;
annotation Annot3 v3 on parameter;
annotation Annot4 v4 on return;

public function func(@v1 { val1: "ballerina" } int id,
                     @v2 { val2: "v3 value defaultable" } string s = "hello",
                     @v3 { val3: "v4 value rest" } float... others) returns @v4{
                        
                        val4: "v4 value defaultable"
                     } float {
    return 1.0;
}