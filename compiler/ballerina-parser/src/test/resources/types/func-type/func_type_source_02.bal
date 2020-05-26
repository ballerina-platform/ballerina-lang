function() returns int f1 = x;
function() returns int|string f2 = x;
function() returns function(int a, int b = c, int... d) f3 = x;

function foo() {
    function() returns int f1 = x;
    function() returns int|string f2 = x;
    function() returns function(int a, int b = c, int... d) f3 = x;
}
