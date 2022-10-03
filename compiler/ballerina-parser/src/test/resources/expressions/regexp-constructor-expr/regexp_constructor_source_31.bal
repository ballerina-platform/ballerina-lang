function foo() {
    x = re `(a)`;
    x = re `.(a).`;
    x = re `.(a)(.)`;
    x = re `.(\w\w)`;
    x = re `(\w\w\w)`;
    x = re `(\w\w)(\w)`;
    x = re `(\w\w)(\W)?`;
    x = re `b(c)`;
    x = re `b(c)?`;
}
