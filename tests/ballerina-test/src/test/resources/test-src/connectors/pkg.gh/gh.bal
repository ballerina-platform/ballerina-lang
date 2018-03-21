package pkg.gh;

import pkg.ij;

public connector Bar (ij:Foo c, string para1) {
    action get1(string path)(string) {
        endpoint<ij:Foo> en {
            c;
        }
        return "Bar-" + path + para1 + en.get(path);
    }
}

function incompatibleEndpoint() {
    endpoint<ij:Foo> ep {
        create Bar(create ij:Foo(), "ddd");
    }
}