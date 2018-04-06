function testUndeclaredStructAccess() {        
    string name;        
    dpt1[name] = "HR";      
}

function testUndeclaredAttributeAccess() {     
    string name;        
    Department dpt = {};
    dpt["id"] = "HR";       
}     
      
type Department {
    string dptName;     
    int count;      
}