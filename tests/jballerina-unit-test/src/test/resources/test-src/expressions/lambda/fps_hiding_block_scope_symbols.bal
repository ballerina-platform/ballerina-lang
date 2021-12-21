function name(int y) returns function (int) returns int {

    var func = function (int y) returns int {
        return y + y;
    };

    return func;
}

function nameArrow(int y) returns int {
    function (int, int) returns int lambda = (x, y) => x + y;
    return lambda(12, 32);
}

int z = 34;
function nameP() returns function (int) returns int {

    var func = function (int z) returns int {
        return z + z;
    };

    return func;
}

function nameArrowP() returns int {
    function (int, int) returns int lambda = (x, z) => x + z;
    return lambda(12, 32);
}
