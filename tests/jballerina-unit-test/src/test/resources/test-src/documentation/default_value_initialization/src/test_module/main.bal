# Represents Foo object
# 
# + i - Expected iter
public type Foo object {
    public int i = 1;
};

# Represents Bar object
# 
# + i - Expected iter
# + s - String str
# + foos - Foo object inside 
public type Bar object {
    public int i = 1;
    public string s = "str";
    public Foo foos = new;
};

# Represents student object
# 
# + f - Bar type object cast inside student
# + name - Name of the student
public type Student object {
    public Foo f = new Bar();
    public string name = "John";
};
