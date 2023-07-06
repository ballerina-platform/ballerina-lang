import ballerina/module1;

function test(string command) {
    int id  = object {
        int id = 10;
        function getId() returns int {
            return self.id;
        }
    }.
}
