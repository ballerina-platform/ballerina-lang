public function main(string... args) {
    TestObject testO = new({
            outerRecField1: 0,
            outerRecField2: "",
            a
            innerRec: {
                inner2Rec: {
                    
                }
            }
        });
}

public function testFunction(OuterRec rec) {
    int a = 12;
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

type TestObject object {
    public function init(OuterRec arg) {
        
    }
};