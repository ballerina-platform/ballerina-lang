type Foo object {
    int[] & readonly x;
    readonly & int[] y;
    readonly Foo|Bar e;
    readonly Foo|int f;
};
