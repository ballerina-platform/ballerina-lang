package pkg.variable;

public int ^"Variable Int" = 800;
public string ^"Variable String" = "value";
public float ^"Variable Float" = 99.34323;
public any ^"Variable Any" = 88343;
public Person ^"person 1" = {^"first name": "Harry", ^"last name":"potter", ^"current age": 25};

public type Person {
    string ^"first name";
    string ^"last name";
    int ^"current age";
};
