public function main(string... args){
    int x = 1;
    string s = "ten";
    int i = 10;
    int j = 20;
    string a = x == 1 ? s : i;
    any b = x ? s : i;
    any c = true ? s : i;
}


function test1 (int value) {
    string s1 = value > 40 ? true : false ? "morethan40" : "lessthan20";
}

function test2() {
    int[] a = [];
    boolean trueVal = true;
    boolean falseVal = false;

    a.push(trueVal ? 1 : "");

    byte[] b = [];
    b.push(falseVal ? 0 : 256);
}
