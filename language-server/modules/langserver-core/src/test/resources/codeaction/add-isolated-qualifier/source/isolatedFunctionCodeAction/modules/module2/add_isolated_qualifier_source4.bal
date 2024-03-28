import ballerina/module1;

service / on new module1:Listener(9090) {
    private int[] items = [];

    isolated resource function name .() {
        var isOdd = self.items.'map(function(int n) returns boolean {
            return n % 2 != 0;
        });
    }
}
