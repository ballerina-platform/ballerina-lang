package pkg.ef;

public connector Foo() {
    action get(string path)(string) {
        return "Foo-" + path;
    }
}

public connector Bar (Foo c, string para1) {
    action get1(string path)(string) {
        endpoint<Foo> en {
            c;
        }
        return "Bar-" + path + para1 + en.get(path);
    }
}
