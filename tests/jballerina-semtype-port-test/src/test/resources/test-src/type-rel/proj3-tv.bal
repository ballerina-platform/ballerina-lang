type IB int|boolean;
type SB string|boolean;
type C string:Char;
type NonC string & !C;

// @type R[C] = IB
// -@type R[NonC] = SB
type R record {|
    int a;
    int b;
    string fieldName;
    boolean...;
|};
