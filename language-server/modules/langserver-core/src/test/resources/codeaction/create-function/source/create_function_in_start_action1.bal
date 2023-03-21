type Foo record {|
    int 'type;
|};

public function function1() {
    Foo & readonly val = {'type: 0};
    future<Foo & readonly> futureResult2 = start createAnotherVal(val); 
}
