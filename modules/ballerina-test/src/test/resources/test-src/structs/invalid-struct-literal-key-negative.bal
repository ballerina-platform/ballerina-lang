struct Department {
    string dptName;
    boolean y;
}

function testInvalidFieldNameInit () {
    string name;
    Department dpt = {dptName[0]:54};
}
