public function foo() {
    table<Myrecord> a;
    table<int> a;
    table<Myrecord> key() a;
    table<Myrecord> key(id) a;
    table<Myrecord> key(id, name) a;
    table<Myrecord> key<int> a;
    table<Myrecord> key<MyId> a;
}
