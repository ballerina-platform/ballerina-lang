public function cloneWithParams() {
    int a = 4;
    int x = a.clone(true);
}

public function cloneInvalidType() {
    typedesc a = int;
    typedesc x = a.clone();
}
