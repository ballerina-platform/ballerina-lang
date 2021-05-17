type Foo record {|
    int i = 1;
|};

public function function1() {
    future<int> f1 = start testAsync();

    future<(Foo & readonly)|int> futureResult = start createVal(10);

    future<Foo & readonly> futureResult2 = start createAnotherVal(10);
}
