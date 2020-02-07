type RecordName1 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
    } sdd;
};

type RecordName2 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
        record {int jd = 0;} hgs;
    } sdd;
};

type RecordName3 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
        record {
            int jd = 0;
            record {
                int hyf = 0;
            } hgt;
        } hgs;
    } sdd;
};

type RecordName4 record {
    int a = 0;
    string s;
    record {|int hd = 0;|} sdd;
};

type RecordName5 record {
    int a = 0;
    string s;
    record {|
        int hd = 0;
    |} sdd;
};

type RecordName6 record {
    int a = 0;
    string s;
    record {|
        int hd = 0;
        string...;
    |} sdd;
};

type RecordName7 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
        record {|int jd = 0;|} hgs;
    } sdd;
};

type RecordName8 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
        record {|
            int jd = 0;
        |} hgs;
    } sdd;
};

type RecordName9 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
        record {|
            int jd = 0;
            int...;
        |} hgs;
    } sdd;
};

type RecordName10 record {
    int a = 0;
    string s;
    record {|
        int hd = 0;
        string...;
    |} sdd;
};

type RecordName11 record {
    int a = 0;
    string s;
    record {|
        int hd = 0;
        string...;
    |} sdd;
};

type Record12 record {
    record {
        string name;
        record {
            string sunrise;
            string sunset;
        } inner;
    }[] outter;
};

function testRecordTypes3() returns [typedesc<RecordA>, typedesc<record {}>] {
    typedesc<RecordA> a = RecordA;
    typedesc<record {}> b = record {
        string c;
        int d;
    };
    typedesc<record {}> b1 =
    record {string c; int d;};
    return [a, b];
}

function testRecordTypes4() returns [typedesc<RecordA>, typedesc<record {}>] {
    typedesc<RecordA> a = RecordA;
    typedesc<record {}> b = record {string c; int d;};
    typedesc<record {}> b1 =
    record {
        string c;
        int d;
    };
    return [a, b];
}
