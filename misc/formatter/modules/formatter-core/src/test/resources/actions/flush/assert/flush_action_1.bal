public function foo() {
    worker w1 {
        float k = 2.34;
        k -> w2;
        error? flushResult = flush w2;
    }
    worker w2 {
        float mw;
        mw = <- w1;
    }
}
