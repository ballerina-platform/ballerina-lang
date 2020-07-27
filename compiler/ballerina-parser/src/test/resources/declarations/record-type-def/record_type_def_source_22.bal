type Foo record {
    int[] & readonly x = [1, 2];
    readonly & int[] y = [1, 2];
    readonly Foo|Bar e;
    readonly Foo|int f;
};
