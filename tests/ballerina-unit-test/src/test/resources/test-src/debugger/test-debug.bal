public function main(string... args){
    int p = 15;
    int q = 5;
    // Invoke Some random Logic.
    int r = calculateExp1(p , q);
    p = p - q;
    // Invoke another random Logic.
    string s = testCalculateExp2(p);
    s = "done";
}
function calculateExp1(int x, int y) returns (int) {
    int z = 0;
    int a = y;
    while(x >= a) {
        a = a + 1;
        if(a == 10){
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}
function calculateExp2(int a, int b, int c) returns (int, int) {
    int x;
    x = 10;
    int e = a;
    if (e == b) {
        e = 100;
    } else if (e == b + 1){
        e = 200;
    } else  if (e == b + 2){
        e = 300;
    }  else {
        e = 400;
    }
    int d = c;
    return (e + x, d + 1);
}
function testCalculateExp2 (int x) returns (string) {
    var (v1, v2) = calculateExp2(x, 9, 15);
    if (v1 > 200) {
        return "large";
    } else {
        return "small";
    }
}