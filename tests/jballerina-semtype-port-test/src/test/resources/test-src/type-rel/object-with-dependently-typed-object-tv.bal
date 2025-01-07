// @type Bar < Baz
type Bar object {
    public function get(typedesc<anydata> td) returns td|error;
};

type Baz object {
    public function get(typedesc<anydata> td) returns anydata|error;
};
