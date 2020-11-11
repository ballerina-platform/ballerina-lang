// Detail Types
type Detail record {
    string x;
};

type DetailTwo record {|
    int x?;
    string...;
|};

type DetailThree record {
    int x;
};

type DetailTypeFour map<string>;

// Error Types
type ErrorOne error<Detail>;

type ErrorTwo error<DetailTwo>;

type ErrorThree error<DetailThree>;

type ErrorFour error<record { int x; }>;

type ErrorFive error<DetailTypeFour>;

//Intersection Types
type IntersectionError ErrorOne & ErrorTwo;

type IntersectionErrorTwo ErrorOne & ErrorThree;

type IntersectionErrorThree ErrorOne & ErrorFour;

type IntersectionErrorFour ErrorOne & ErrorFive;

function testRecordAndMapIntersection() {
    var err = IntersectionErrorFour("message", x = "x", z = 10);
}
