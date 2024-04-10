isolated class IsolatedClass {
    record {|
        string id;
        int count;
    |} rec;

    function init() {
        self.rec = {count: 0, id: ""};
    }
}
