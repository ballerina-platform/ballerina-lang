function foo() {
    var x1 = a? b? c:d:e;
    var x2 = a? b? c:d.e:f;
    var x3 = a? b? c.d:e:f;

    var y1 = a? b? c : d : e;
    var y2 = a? b? c:d.e : f;
    var y3 = a? b? c.d:e : f;

    int b11 = cond1? cond2? cond3? y5 : x5 : x5 : x5;
    int res = 5 == 5 ? 10 < 5 ? 100 : n : -1;
}
