type Type1 object { 
    string target; 
    public function doSomething(); 
};

type Type2 readonly & Type1;

Type2 obj = object {
    string target = "abcd";
    public function doSomething() {}
};

function name() {
    var _ = obj.
}
