type ResConfigData record {|
    string scopes;
|};

annotation ResConfigData ResConfig on object function;

service / on new Listener(9090) {
    
    @ResConfig {
        scopes: ""
    }
    resource function get . () {
        
    }
}
