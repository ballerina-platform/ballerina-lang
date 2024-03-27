isolated class IsolatedClass {
    function (int i) returns boolean fn;

    function init(function (int i) returns boolean fn) {
        self.fn = fn;
    }
}
