type Foo object {

    private age; // private qualifier not allowed

    string name = "Lochana"; // field-initializer not allowed

    // method-defn not allowed
    function bar () {
        int n;
        function baz() {
           int q;
        }
    };
};
