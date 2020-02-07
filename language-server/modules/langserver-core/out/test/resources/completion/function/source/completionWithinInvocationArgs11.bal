type TestObject object {
    int testVal1;
    OuterRec testVal2;

    public function __init(int arg1, OuterRec arg2 = {}) {
        self.testVal1 = arg1;
        self.testVal2 = arg2;
    }
};

public function main(string... args) {
    TestObject testObj = new(12, arg2 = {
        outerRecField1: 0,
        outerRecField2: "",
        innerRec: {
            
        }
    });
}

type OuterRec record {
    int outerRecField1 = 111;
    string outerRecField2 = "Test";
    InnerRec innerRec = {};
};

type InnerRec record {
    string innerRecField1 = "";
    int innerRecField2 = 12;
    Inner2Rec inner2Rec = {};
};

type Inner2Rec record {
    int inner2Int = 12;
    string inner2Str = "";
};