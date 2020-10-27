function foo() {
    x = from int a in b
        let var { a, b:[p, _, ...s], ...d } = d
        select e;
    x = from int a in b
        let T { } = d
        select e;
    x = from int a in b
        let T { ...d } = d
        select e;
}
