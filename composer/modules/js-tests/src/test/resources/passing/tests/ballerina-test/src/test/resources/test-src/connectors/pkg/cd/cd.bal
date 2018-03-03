package pkg.cd;

import pkg.ef;

public connector BarFilter1 (ef:Bar c, string para1) {
    action get1(string path)(string) {
        endpoint<ef:Bar> en {
            c;
        }
        return "BarFilter1-" + path + para1 + en.get1(path);
    }
}

public connector FooFilter1 (ef:Foo c, string para1) {
    action get(string path)(string) {
        endpoint<ef:Foo> en {
            c;
        }
        return "FooFilter1-" + path + para1 + en.get(path);
    }
}
