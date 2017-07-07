$ ballerina run main xml.bal
# Type of the XML
comment
# Name of the XML
name
# Text content of the XML
Book1
# XML after adding an attribute
<book xmlns:ns0="http://ballerina.com/" ns0:year="2017"></book>
# Attribute value
2017
# Concatenated XML
Hello, World!<name>Book1</name><!-- comment about the book-->
<?doc document="book.doc" ?>
# Is the XML emtpy?
false
# Has only one element?
false
# Subsequence of a sequence
<!-- comment about the book--><?doc document="book.doc" ?>
# All the element type items
<name>Book1</name>
# Selected element
<name>Book1</name>
# XML after setting the children
<book xmlns:ns0="http://ballerina.com/" ns0:year="2017">Hello, World!
<name>Book1</name><!-- comment about the book--><?doc  document="book.doc" ?>
</book>
# All the children
Hello, World!<name>Book1</name><!-- comment about the book-->
<?doc document="book.doc" ?>
# Selected child
<name>Book1</name>
# Stripped XML
Hello, World!<name>Book1</name><!-- comment about the book-->
<?doc document="book.doc" ?>
# Coppied XML
<!-- comment about the book-->