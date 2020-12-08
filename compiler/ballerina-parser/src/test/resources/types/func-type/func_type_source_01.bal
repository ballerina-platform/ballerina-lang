function() f1 = x;
function(int a, int b = c, int... d) f2 = x;
function(int, int = c, int...) f3 = x;

function foo() {
    function() f4;
    function(int a, int b = c, int... d) f5;
    function(int, int = c, int...) f6 = x;
}

function()|() a = ();
function()? b = ();
final function() c = ();
