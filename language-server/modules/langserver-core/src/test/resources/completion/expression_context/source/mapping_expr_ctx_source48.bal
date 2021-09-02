import ballerina/module1;

function name() {
    map<RecordName> recMap;
    recMap = {
        "rec1": {
            
        }
    };
}

type RecordName record {
    RecordName2 rec1f1;
    int rec1f2;
};

type RecordName2 record {
    int rec2f1;
    int rec2f2;
};
