type User record {|
    string name;
|};

type Payload record {|
    record {|
        *User;
        string cardNo;
    |} patient;
|};

type Doctor record {
    int category;
    string name;
};

type Appointment record {
    record {
        *Doctor;
        int id;
    } doctor;
};

function foo() {
    Payload payload = {
        patient: {name: "jack", cardNo: "1"}
    };
    Payload {patient: {...patient}} = payload;
    _ = patient.cardNo;

    Appointment appointment = {doctor: {id: 0, category: 0, name: "jack"}};
    Appointment {doctor: {...doctor}} = appointment;
    _ = doctor.category;

    record {|record {|string s1; int x1;|} x; int y;|} anonRec1 = {x: {x1: 1, s1: "s1"}, y: 2};
    record {|record {|string s1; int x1;|} x; int y;|} {x: {...x2}} = anonRec1;
    _ = x2.s1;

    record {record {string s1; int x1;} x; int y;} anonRec2 = {x: {x1: 1, s1: "s1"}, y: 2};
    record {record {string s1; int x1;} x; int y;} {x: {...x3}} = anonRec2;
    _ = x3.s1;
}

public function main() {
    foo();
}
