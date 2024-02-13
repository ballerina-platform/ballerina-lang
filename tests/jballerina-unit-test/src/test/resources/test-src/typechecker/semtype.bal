type Rec record {|
    int value;
|};
public function test() {
    int|boolean|Rec a = 5;
    if a is boolean|Rec {
        panic error("unexpected");
    }
}
