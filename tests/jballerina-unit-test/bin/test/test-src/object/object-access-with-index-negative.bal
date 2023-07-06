function testUndeclaredStructAccess() {
    string name = "";
    dpt1[name] = "HR";
}

function testUndeclaredAttributeAccess() {     
    string name = "";
    Department dpt = {};
    dpt["id"] = "HR";       
}     
      
class Department {
    public string dptName = "";
    public int count = 0;
}
