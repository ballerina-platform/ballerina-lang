#At the command line, navigate to the directory that contains the 
#`.bal` file and run the `ballerina run` command. 
$ ballerina run xml_attributes.bal
<ns0:book xmlns:ns0="http://ballerina.com/aa" ns0:status="available" count="5"></ns0:book>
available
available
5
5
Not Available
{"{http://www.w3.org/2000/xmlns/}ns0":"http://ballerina.com/aa", "{http://ballerina.com/aa}status":"Not Available", "count":"5"}
{"{http://www.w3.org/2000/xmlns/}ns0":"http://ballerina.com/aa", "{http://ballerina.com/aa}status":"Not Available", "count":"5"}
5
