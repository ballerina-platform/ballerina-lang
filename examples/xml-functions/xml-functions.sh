$ ballerina run xml-functions.bal
# Type of the XML element.
comment
# Name of the XML element.
name
# Text content of the XML element.
Book1
# Is the XML emtpy?
false
# Does it have only one element?
false
# Subsequence of a sequence.
<!--some comment-->
# All the element type items.
<name>Book1</name>
# Selected element.
<name>Book1</name>
# XML after setting the children.
<book>Hello, World!<name>Book1</name><!--some comment--></book>
# All the children.
Hello, World!<name>Book1</name><!--some comment-->
# Selected child.
<name>Book1</name>
# Stripped XML.
Hello, World!<name>Book1</name><!--some comment-->
# Copied XML.
<!--some comment-->
