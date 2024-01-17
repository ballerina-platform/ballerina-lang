import ballerina/module1;

service on new module1:Listener(9090) {
    resource function get .() returns int[] {
        int[] arr = [1, 2, 3];
        return from int i in arr
            
            select i;
    }
}
