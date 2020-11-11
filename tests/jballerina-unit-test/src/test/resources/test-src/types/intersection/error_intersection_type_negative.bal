type Detail record {
    string x;
};

type DetailTwo record {|
    int x?;
    string...;
|};

type ErrorOne error<Detail>;

type ErrorTwo error<DetailTwo>;

type IntersectionError ErrorOne & ErrorTwo;
