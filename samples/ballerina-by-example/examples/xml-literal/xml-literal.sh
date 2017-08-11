$ ballerina run xml-literal.bal
<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>
<book xmlns="http://ballerina.com/" xmlns:ns0="http://ballerina.com/aa" ns0:status="available">
                    <ns0:name>Sherlock Holmes</ns0:name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>
<newBook xmlns="http://ballerina.com/" xmlns:ns0="http://ballerina.com/aa">
                    <name>Sherlock Holmes</name>
                    <author>(Sir) Arthur Conan Doyle</author>
                    <!--Price: $ 12-->
                  </newBook>
