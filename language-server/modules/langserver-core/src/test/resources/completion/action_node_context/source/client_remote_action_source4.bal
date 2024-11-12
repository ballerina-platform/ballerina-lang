client class ClientClass {
    resource function accessor ["path" a] () {
        
    }
}

public function main() {
    var cl = new ClientClass();
    cl ->/["path" a].accessor;    
}
