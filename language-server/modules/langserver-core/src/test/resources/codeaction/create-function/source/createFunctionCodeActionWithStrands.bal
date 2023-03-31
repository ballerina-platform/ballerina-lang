type Foo record {|
    int i = 1;
|};

public function function1() {
    future<int> f1 = start testAsync();
    int a = 10;
    future<(Foo & readonly)|int> futureResult = start createVal(a);
    Foo & readonly val = {};
    future<Foo & readonly> futureResult2 = start createAnotherVal(val);
    start funcWithNoType()
}
