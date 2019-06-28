import ballerina/mysql;

type Person object {
    string name = "";

    function __init(string name = "", record {int id = 0; int address = "";} details = {}) {
        self.name = name;
        var detailRec = details;
    }
}

function name() {
    mysql:Client studentDb1 = new ({
        host: "localhost",
        port: 5690,
        name: "testdb",
        username: "root",
        password: "",
        dbOptions: {useSSL: false}
    });

    mysql:Client studentDb2 = new ({host: "localhost", port: 5690, name: "testdb", username: "root", password: "", dbOptions: {useSSL: false}});

    mysql:Client studentDb3 = new ({
        host: "localhost",
        port: 5690,
        name: "testdb",
        username: "root",
        password: "",
        dbOptions: {useSSL: false}
    });

    Person p1 = new Person("", {id: 0, address: ""});
    Person p2 = new;
    Person p3 = new ("", {id: 0, address: ""});
    Person p4 = new Person("", {
        id: 0,
        address: ""
    });
    Person p5 = new ("", {
        id: 0,
        address: ""
    });

    mysql:Client studentDb4 =
    new
    ({
        host: "localhost",
        port: 5690,
        name: "testdb",
        username: "root",
        password: "",
        dbOptions: {useSSL: false}
    })
    ;
}
