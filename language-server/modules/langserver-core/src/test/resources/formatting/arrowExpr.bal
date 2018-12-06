function testArrowExprWithTwoParams() {
    function (int, string) returns string lambda1 =                ( x,    y)=>        x + y;

    function (int) returns int lambda2 =( x )=>x + 1 + 1;

    function (int) returns int lambda3 =x=>x + 1;

    function () returns int lambda4 =(   )      =>        1 + 1;

    // Height preserving test

    function (int, string) returns string lambda5 =
       (x,y)=>x + y;

    function (int) returns int lambda6 =
 (x )=>x + 1 + 1;

    function (int) returns int lambda7 =
         x=>    x + 1;

    function () returns int lambda8 =
()=>1 + 1;
}