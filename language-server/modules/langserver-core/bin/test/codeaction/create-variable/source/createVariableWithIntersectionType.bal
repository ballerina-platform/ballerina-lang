type Foo record {|
    int i = 1;
|};

function recordIntersection(Foo foo) returns (readonly & Foo)|int|(string[] & readonly) => foo.cloneReadOnly();

public function function1() {
    recordIntersection({});

    getIntersectionValue();
}

function getIntersectionValue() returns Foo & readonly {
    return {};
}
