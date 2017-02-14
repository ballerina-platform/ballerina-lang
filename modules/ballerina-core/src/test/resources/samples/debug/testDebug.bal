function main(string[] args){
    int p = 15;
    int q = 5;
    // Invoke Some random Logic.
    int r = calculateExp1(p , q);
    p = p - q;
    // Invoke anothe random Logic.
    string s = testCalculateExp2(p);
    s = "done";
}
function calculateExp1(int x, int y) (int) {
    int z;
    while(x >= y) {
        y = y + 1;
        if(y == 10){
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}
function calculateExp2(int a, int b, int c) (int, int) {
    int x;
    x = 10;
    if ( a == b) {
        a = 100;
    } else if (a == b + 1){
        a = 200;
    } else  if (a == b + 2){
        a = 300;
    }  else {
         a = 400;
    }
    b = c;
    return a + x, b + 1;
}
function testCalculateExp2(int x) (string) {
    int v1;
    int v2;
    v1, v2 = calculateExp2(x, 9, 15);
    if (v1 > 200) {
        return "large";
    } else {
        return "small";
    }
    return "Unknown";
}
service EchoService{
  resource echoResource (message m) {
      reply m;
  }
}
