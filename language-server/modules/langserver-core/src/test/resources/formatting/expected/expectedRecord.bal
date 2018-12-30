type RecordName1 record {
    int a = 0;
    string s;
};

public type RecordName2 record {
    int a = 0;
    string s;
};

public type RecordName3 record {

};

type RecordName4 record {

};

type RecordName5 record {
    int a = 0;
    string s;
    record {
        int hd = 0;
    } sdd;
};

public type RecordName6 record {
    int a = 0;
    string s;
    !...
};

public type RecordName7 record {
    int a = 0;
    string s;
    string...
};

type RecordName8 record {
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

function name1() {
    record {
        int hd = 0;
        record {
            int jd = 0;
            record {
                int hyf = 0;
            } hgt;
        } hgs;
    } sdd;
}
