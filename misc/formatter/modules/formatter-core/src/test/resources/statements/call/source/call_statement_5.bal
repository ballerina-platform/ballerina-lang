public function main() {
    foo(object {
        int i = 1;
    }, a,
    b);
}

public function bar() {
    bar(t1, object {
         int i = 1;
        }, t2, t3);
}

public function baz() {
    baz(t1, t2, object {
      int i = 1;
     int y = 2;
    },
    b,
    c,
    d);
}

public function fox() {
    foz(t1,
        object {
            int i = 1;
        },
        b,
        c,
        d);
}
