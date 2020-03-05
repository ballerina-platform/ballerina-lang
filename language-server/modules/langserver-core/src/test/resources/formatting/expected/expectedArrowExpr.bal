function testArrowExprWithTwoParams() {
    function (int, string) returns string lambda1 = (x, y) => x + y;

    function (int) returns int lambda2 = (x) => x + 1 + 1;

    function (int) returns int lambda3 = x => x + 1;

    function () returns int lambda4 = () => 1 + 1;

    // Height preserving test

    function (int, string) returns string lambda5 =
        (x, y) => x + y;

    function (int) returns int lambda6 =
        (x) => x + 1 + 1;

    function (int) returns int lambda7 =
        x => x + 1;

    function () returns int lambda8 =
        () => 1 + 1;

    // Height preserving test 2
    function (int, string) returns string lambda9 =
        (
            x
            ,
            y
        )
        =>
            x
            +
            y
    ;

    function (int) returns int lambda10 =
        (
            x
        )
        =>
            x
            +
            1
            +
            1
    ;

    function (int) returns int lambda11 =
        x
        =>
            x
            +
            1
    ;

    function () returns int lambda12 =
        (
        )
        =>
            1
            +
            1
    ;
}
