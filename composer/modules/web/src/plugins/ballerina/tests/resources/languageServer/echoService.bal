
import ballerina/http;

int globalInt = 0;
@http:configuration {basePath:"/echo"}
service<http> echo {
    
    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {

        int x= 0;
        boolean done = true;
        if (done) {
            int y = 0;
            string name = "John";
            
        }
        
        if(done){
            int z = 0;
            string colour = "Black";
            
        }
        
        while (done) {
           int j = 0;
           string shape = "Circle";
           
        }
        
        http:convertToResponse(m);
        reply m;
    }
    
}
