public function main(string... args) {
    OuterRec rec = {
        outerRecField1: 0,
        outerRecField2: "",
        innerRec: {
            inner2Rec: {
                
            }
            
        }
        
    };
}

type OuterRec record {
    int outerRecField1 = 111;
    string outerRecField2 = "Test";
    InnerRec innerRec;
};

type InnerRec record {
    string innerRecField1 = "";
    int innerRecField2 = 12;
    Inner2Rec inner2Rec;
};

type Inner2Rec record {
    int inner2Int = 12;
    string inner2Str = "";
};
