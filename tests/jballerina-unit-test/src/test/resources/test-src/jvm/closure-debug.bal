type F function(int) returns int;

public function main() {
    int a = 5;
    int b = 4;
    function(int) returns int f = function(int x) returns int {
        F inner = function(int base) returns int {
            return b * base;
        };
        return inner(x) + a;
    };
    int c = f(5);
}
