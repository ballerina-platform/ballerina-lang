function foo() {
    wait {a};
    wait {a: b};
    wait {a, c: d:e};

    x = wait {a};
    x = wait {a: b};
    x = wait {a, c: d:e};
}
