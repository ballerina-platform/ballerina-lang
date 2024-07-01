// @type O1 = O2
type O1 object {
    O1? other;
};

type O2 object {
    O2? other;
};

// @type O4 < O3
type O3 object {
    function foo(O3 other);
};

type O4 object {
    function foo(O3 other);
    function bar(O4 other);
};
