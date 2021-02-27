type Student record {
    string Name;
    int Grade;
    map<any> Marks;
};

# Define a closed record type named `Address`. The `{|` and `|}` delimiters indicate that this record type
# allows mapping values, which contain only the described fields.
type Address record {|
    string city;
    string country;
|};

# Description
#
# + age - age Parameter Description
# + firstName - firstName Parameter Description
# + lastName - lastName Parameter Description
type Person object {
    public int age;
    public string firstName;
    public string lastName;

    // Returns full name
    function getFullName() returns string;

    # Description
    #
    # + condition - condition Parameter Description
    # + a - a Parameter Description
    function checkAndModifyAge(int condition, int a);
};
