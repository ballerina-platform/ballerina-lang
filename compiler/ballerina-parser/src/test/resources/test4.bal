type Student record {|
    *Person;
    string name;
    *Human;
    int age;
    
    record {
        *Person;
        string name;
        *Human;
        int age;
        
        float...;
        
        int score;
    } parent;

    float...;
    
    int score;
    
|};

