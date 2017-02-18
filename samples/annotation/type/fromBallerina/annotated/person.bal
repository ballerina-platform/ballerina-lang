package sample.annotation.type.fromBal.annotated;

@TypeInfo( name = "person" )
@Xml( name = "person" , namespace = "http://example.com/person" , prefix = "per" , xsdType = "personType" )
type Person{

    @Property( required = true , title = "User ID of the person." )
    @Xml( attribute = true )
    int userID;

    @Property( required = true , title = "Name of the person." )
    string name;

    @Property( format = "date" )
    @XML( namespace = "http://www.w3.org/2001/XMLSchema" , xsdType="date" )
    string birthday;

    Address address;

    @Property( format = "email" )
    string[] email;

}

@TypeInfo( name = "address" )
@Xml( name = "addressXML" , namespace = "http://example.com/person" , prefix = "per" , xsdType = "addressType" )
type Address{

    @Property( required = true )
    string addressLine;

    @Property( required = true )
    string city;

    string state;

    @Property( required = true )
    string country;

    @XML( namespace = "http://www.w3.org/2001/XMLSchema" , xsdType="int" )
    int zipcode;

}