# Type descriptor type param
public type TypeDescType typedesc;

# Any type param
public type AnyType any;

# Any data type param
public type AnyDataType anydata;

# Distinct error type param
public type IOError distinct error;

# Open record type param
# #
# + fileName - file name
public type FileErrorDetail record {
    string fileName;
};

# Error type param
public type ErrorName error<map<FileErrorDetail>>;

# Closed record type param
#
# + firstName - first name of the student
# + lastName - last name of the student
# + age - age of the student
type Student record {|
    readonly string firstName;
    string lastName;
    int age;
|};


# Table type param
public type TableType table<Student>;

# Table type param with key
public type TableTypeWithKey table<Student> key<firstName>;

# Map type param
public type MapType map<int>;

# Record type param
#
# + firstName - first name of the student
# + lastName - last name of the student
# + age - age of the student
public type RecordType record {|
    readonly string firstName;
    string lastName;
    int age;
|};

# Readonly record type param
#
# + firstName - first name of the student
# + lastName - last name of the student
# + age - age of the student
public type ReadonlyRecordType readonly & record {|
    string firstName;
    string lastName;
    int age;
|};

# Readonly record type param 2
#
# + firstName - first name of the student
# + lastName - last name of the student
# + age - age of the student
public type RecordReadonlyType record {|
    string firstName;
    string lastName;
    int age;
|} & readonly;

# Optional record type param
#
# + firstName - first name of the student
# + lastName - last name of the student
# + age - age of the student
public type RecordTypeOptional record {|
    string firstName;
    string lastName;
    int age;
|} ?;

# Error intersection type param
public type FileIOError IOError & error<FileErrorDetail>;

# Readonly intersection type param
public type ReadonlyIntersectionType readonly & string;

# Array type param
public type ArrayType int[];

# Union type param
public type UnionType int|error;

# Tuple type param
public type TupleType [string, int];

# Object type param
public type ObjectType object {
    # This method will be called to get name of the student
    # + id - student id
    # + return - name of the student
    public function getName(int id) returns string;

    # This method will be called to get class name of the student
    # + id - student id
    # + return - class name of the student
    public function getClassName(int id) returns string;
};

# Distinct object type param
public type DistinctObjectType distinct object {};

# Client object type param
public type ClientObjectType client object {};

# Service object type param
public type ServiceObjectType service object {};

# Integer type param
public type IntegerType int;

# String type param
public type StringType string;

# Decimal type param
public type DecimalType decimal;

# Stream type param
public type StreamType stream<Student>;

# Function type param
public type FunctionType function;

# Deprecated union string type param
# # Deprecated
# This type is deprecated
@deprecated
public type DeprecatedType "on"|"off";
