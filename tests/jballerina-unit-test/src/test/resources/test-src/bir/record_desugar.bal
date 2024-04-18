type R1 record {|
    int x;
|};

type R2 record {|
    int? x;
|};

type R3 record {|
    int x?;
|};

function setRequiredField() {
    R1 r1 = {x: 1};
    r1.x = 2;
}

function setNillableField() {
    R2 r2 = {x: 1};
    r2.x = 2;
    r2.x = ();
}

function setOptionalField() {
    R3 r3 = {};
    r3.x = 2;
    r3.x = ();
}
