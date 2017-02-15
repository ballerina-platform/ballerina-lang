# Types, Variables & Constants

The Ballerina type system has value types and reference types. Ballerina comes with a set of built-in reference types and array, struct or iterator type constructors to create new reference types.

The type system is illustrated in the following:

![Ballerina Type System](images/typesystem.png)

The rest of this section explains the semantics of these types in detail.

## Declaring & Initializing Variables

Variables declarations are considered statements in the language and can be interspersed with other statements in any order.

A `VariableDeclaration` has the following structure:

```
TypeName VariableName [ = Expression];
```

Variables can be initialized using the standard literal value syntax for that type of variable or using expressions consisting of literal values and any other variables that are in-scope and already initialized. See the 'Literal Values' subsection below for the syntax for literal values for value types, built-in reference types and user defined reference types.

## Allocating & Deallocating Variables

All value typed variables are allocated on the stack, while all reference typed variables are allocated on the heap. Value typed variables are deallocated when they go out of scope and all reference typed variables are garbage collected when they are no longer in use.

As all reference typed variables are allocated on the heap, they must be explicitly allocated. This can be done by assigning them a literal value (see the 'Literal Values' subsection below) or by simply creating an empty value.

## Scoping, Visibility, Shadowing and SSS

Ballerina is a statically scoped language.

## Value Types

Ballerina includes the following value types:
- boolean
- int
- float
- string

The types 'int' and 'float' both support 64-bit IEEE754 arithmetic. The 'boolean' types has only two literal values: 'true' and 'false'. The 'string' type operates similar to value types in that assignment and comparison involve the value and not the pointer.

Value types can be initialized at declaration by assigning a value of that type.

Literal values for these types are written using standard formats for writing integral values and floating point values; the grammar has the exact specification.

## User-Defined Reference Types

###  Structured Types (Records)

User defined record types are defined using the `struct` keyword as follows:
```
struct TypeName {
    TypeName FieldName;+
}
```

Variables of struct types can be initialized at declaration time or later using the following syntax:
```
StructVariableName = { FieldName : Expression, .. FieldName : Expression};
```
This results in a new instance of the struct being created with the named fields assigned the indicated values. If a field is not named then it has no value when the struct is created. Thus, structs with no values assigned can be created by assigning the value '{}'.

##### Arrays

Arrays are defined using the array constructor `[]` as follows:
```
TypeName[]
```
All arrays are unbounded in length and support 0 based indexing. Arrays may be sparse as well and they will grow to meet whatever size needed based on the index (subject to memory avaialability of course).

Array typed variables can

## Built-In Reference Types

Ballerina comes with a pre-defined set of reference types which are key to supporting the types of programs that Ballerina developers are expected to write. These are supported by a set of standard library functions found in the packages 'ballerina.lang.*'. This section defines each of these types and defines their usage.

### Type: 'message'

The 'message' type is an opaque type used to represent a request to a 'resource'. This approach is necessary to make the 'resource' programming model network protocol independent, even though a given 'resource' is always tied to a particular protocol as a 'service' can only be bound to a single network protocol at a time.

Library functions for accessing information from this type are in the package 'ballerina.lang.message'.

A variable of type 'message' can be initialized to hold an empty value as follows:

'''
message VarName = {};
'''

### Type: 'exception'

The 'exception' type is used to hold an exception. An exception contains two properties: a type string and a message string. In addition, library functions can generate a stack trace of the source of the exception. Note that unlike other languages, Ballerina does not allow developers to define subtypes of the exception type and custom exceptions must be thrown by using customer type strings. As such exception type strings starting with "Ballerina:" are reserved for system use only.

Library functions for accessing information from this type are in the package 'ballerina.lang.exception'.

### Type: 'map'

The 'map' type is a hash map from string to any type.

Library functions for accessing information from this type are in the package 'ballerina.lang.map'.

> NOTE: A future version of the language will likely introduce syntax to constrain the value space of a map to a particular type. The syntax under consideration for that is map<TypeName> VariableName. We are currently not considering expanding the key space beyond string.

### Type: 'xml' & 'xmlDocument'

********** NOT FINISHED ***********

#### XML & JSON Types

Ballerina has built-in support for XML elements, XML documents, and JSON documents. TypeName
can be any of the following:
- json\[\<json_schema_name\>\]
- xml\[<{XSD_namespace_name}type_name\>\]
- xmlDocument\[<{XSD_namespace_name}type_name\>\]

A variable of type `json` can hold any JSON document. The optional qualification of the TypeName
for a JSON document indicates the name of the JSON schema that the JSON value is assumed to
conform to.

A variable of type `xml` can hold any XML element. The optional qualification of the TypeName
for an XML document indicates the qualified type name of the XML Schema type that the XML
element is assumed to conform to.

A variable of type `xmlDocument` can hold any XML document, and the optional schema type is the type of the document element.

#### Allocating Variables

Primitive types do not have to be dynamically allocated as they are always allocated
on the stack.

All non-primitive types, user-defined types, and array types have to be
allocated on the heap using `new` as follows:
```
new TypeName[(ValueList)]
```
The optional ValueList can be used to give initial values for the fields of any record type. The order
of values must correspond to the order of field declarations.

#### Default Values for Variables

Variables can be given values at time of declaration as follows:
```
TypeName VariableName = Value;
```

#### Literal Values

The following are examples of literal values for various types:
```
int age = 4;
double price = 4.0;
string name = "John";
xml address_xml = `<address><name>$name</name></address>`;
json address_json = `{"name" : "$name", "streetName" : "$street"}`;
map m = {"name" : "John", "age" : 34 };
int[] data = [1, 2, 3, 6, 10];
```

## Iterators
Iterators are defined using the iterator constructor `~` as follows:
```
TypeName~
```
Iterator typed values are navigated through using an `iterate` statement.

> NOTE: Iterators are still not fully consumated. Iterators are currently only available for the built-in types xml and json. In the future we will allow developers to define their own iterators for their types.


#### Type Coercion and Conversion

The built-in `float` and `double` follow the standard IEEE 754 specifications. The `int` and `long` types follow
the standard 32- and 64-bit integer arithmetic, respectively.

The following lossless type coercions are pre-defined in Ballerina:
- boolean -> int/long/float/double with values 0 or 1 for false or true, respectively
- int -> long/float/double
- long -> double
- float -> double

In addition to these built in type coercions, Ballerina allows one to define
arbitrary conversions from one non-primitive type to another non-primitive and have the language apply it automatically.

A `TypeConvertor` is defined as follows:
```
typeconverter TypeConverterName (TypeName VariableName) (TypeName) {
    VariableDeclaration;*
    Statement;+
}
```
If a TypeConvertor has been defined from Type1 to Type2, it will be invoked by the runtime upon
executing the following statement:
```
Type1 t1;
Type2 t2;

t2 = (Type2) t1;
```

That is, the registered type convertor is invoked by indicating the type cast as above. Note that while
the compiler can auto-detect the right convertor to apply, we have chosen to force the user to
request the appropriate convertor by applying a cast.

##### Built in Type Convertors

In addition to the built-in value type coercions, Ballerina also ships with a few pre-defined type
convertors to make development easier. The following predefined type convertors are declared in
the Ballerina package `ballerina.lang.convertors`:
- string/xml/json to message: creates a new message with the given string/xml/json as its payload
- down conversions for numeral types: int -> boolean (0 is false), long -> int/boolean, float -> int/boolean, double -> float/long/int/boolean,

Note that these must be triggered by indicating a type cast to the desired type.
