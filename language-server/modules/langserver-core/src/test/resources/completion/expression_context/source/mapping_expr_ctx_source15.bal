import ballerina/module1;

function name() {
    record {
        int test = 12;
        RecordName rec;
    } testVar = {
        rec: {
            rec1f1: {
                
            }
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