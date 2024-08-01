class User {
    private string uuid;
    private string name;

    function init(string uuid, string name) {
        this.uuid = uuid;
        this.name = name;
    }
}

final User u1 = new User("123", "Alice");
