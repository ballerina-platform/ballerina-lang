type PersonA record {|
    string fname;
    string lname;
    int age;
};

type PersonB record {
    string fname;
    string lname;
    int age;
|};

type PersonC record { |
    string fname;
    string lname;
    int age;
| };

type EmptyRec1 record {|| };

type EmptyRec2 record { ||};

type EmptyRec3 record { || };

type EmptyRec4 record { | | };
