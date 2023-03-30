import ballerina/module1;

service / on new module1:Listener(9090) {
    private int[] items = [];

    isolated resource function name . () {
        self.items.forEach(function(int item) {
            
        });
    }
}
