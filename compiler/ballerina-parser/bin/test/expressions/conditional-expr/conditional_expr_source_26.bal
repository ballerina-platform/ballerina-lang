public function main() {
    x1 = a ? b : bar();
    x2 = a ? b:bar() : "Ballerina";
    x3 = a ? int : bar();
    x3 = a ? decimal : bar();
    x3 = a ? float : bar();
    x4 = a ? int:bar() : "Ballerina";
    int a = b ? c : d.a();
    int a = b ? c:d.a() : e;
    any a = b ? string : d[1];
    any a = b ? string:d[1] : e;
    int a = b ? <TargetType>b : c;
    int a = b ? <TargetType>b:c : d;
    typedesc a = b ? typeof b : c;
    typedesc a = b ? typeof b:c : d;
    int a = b ? c + boolean : e;
    int a = b ? c + boolean:e : f;
    int a = b ? c + <int>d : e;
    int a = b ? c + <int>d:e : f;
    int i = b ? p.x : p.y;
    boolean i = x ? rec.t : xx:bar();
    var i = x ? a : b:c;
}
