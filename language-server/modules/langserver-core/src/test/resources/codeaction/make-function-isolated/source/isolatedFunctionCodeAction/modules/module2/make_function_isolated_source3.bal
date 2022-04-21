import ballerina/http;

service / on new http:Listener(8080) {
    private int[] items = [];

    isolated resource function name . () {
        self.items.forEach(function(int item) {
            
        });
    }
}
