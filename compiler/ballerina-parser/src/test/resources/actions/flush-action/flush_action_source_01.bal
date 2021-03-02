function foo() {
    flush;
    flush w1;
    flush default;
    flush function;

    x = flush;
    x = flush w1;
    x = flush default;
    x = flush function;
}
