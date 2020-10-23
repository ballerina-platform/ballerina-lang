public function foo() {
    error<NO_MATCHING_OBJECT> a;
    error a;
    error<*> a;
    error<string|float> a;
    error<A[5]> a;
    error<object {}> a;
}
