type PersonA record {|
    string fname;
    string lname;
    int age;
    string ...;
|};

type PersonB record {|
    string fname;
    string lname;
    int age;
    string   ...;
|};

type PersonC record {|
    string fname;
    string lname;
    int age;
    any
    ...;
|};
