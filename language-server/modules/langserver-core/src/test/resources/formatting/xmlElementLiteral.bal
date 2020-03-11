xmlns "http://ballerina.com/b" as ns1;

function testElementLiteralWithNamespaces() returns [xml, xml] {
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/a" as ns0;
    xmlns "http://ballerina.com/c" as ns1;

 xml    x1=xml    `<   root ns0 : id = "456" >< foo >123</ foo >< bar ns1 : status  =  "complete" ></ bar ></ root >` ;xml x2  =  x1  /  *  ;
     return  [ x1 , x2 ];
}

function testElementLiteralWithNamespaces1() returns [xml, xml] {
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/a" as ns0;
    xmlns "http://ballerina.com/c" as ns1;

    xml x1 =
           xml        `<root
         ns0
  :
            id
    =
          "456"><foo>123</foo><bar
             ns1
         :
 status
          =
 "complete"></bar></root>`
        ;
              xml x2 =
           x1
        /
 *
       ;
    return [x1, x2];
}

function testElementWithQualifiedName() returns [xml, xml, xml] {

    xml x1 =     xml      `< root >hello</ root >`     ;

        xmlns      "http://ballerina.com/";
 xml    x2 =    xml       `< root >hello</ root >`;

              xml x3  =xml    `< ns1 : root >hello</ ns1 : root >`       ;

 return    [  x1 , x2 , x3 ]   ;
}

function testElementWithQualifiedName1() returns [xml, xml, xml] {

    xml x1 =
        xml    `< root >hello</ root >`
 ;

    xmlns"http://ballerina.com/" ;
               xml   x2=
       xml      `< root >hello</root>`
        ;

  xml    x3=
        xml     `< ns1 : root >hello</ ns1 : root >`
      ;

    return [x1, x2, x3];
}

function testElementLiteralWithTemplateChildren() returns [xml, xml] {
    string v2 = "aaa<bbb";
    xml x1 =xml`< fname >John</ fname >`  ;
    xml x2 =    xml   `< lname >Doe</ lname >`  ;

 xml   x3=xml     `< root >hello ${  v2 } good morning ${ x1 } ${ x2 }. Have a nice day!< foo >123</ foo >< bar ></ bar ></ root >` ;
    xml    x4=    x3 / * ;
    return  [ x3,x4];
}

function testElementLiteralWithTemplateChildren1() returns [xml, xml] {
    string v2 = "aaa<bbb";
    xml x1 =
    xml`<fname>John</fname>`;
    xml x2    =
xml       `<
          lname
>Doe</
 lname
         >`
 ;

  xml    x3 =
       xml     `<
        root
  >hello ${
          v2
  } good morning ${
        x1
 } ${
        x2
 }. Have a nice day!<
            foo
 >123</
         foo
        ><
       bar
       ></
  bar
        ></
       root
  >`
        ;
    xml x4 =
    x3
    /
    *
    ;
    return [
    x3
    ,
    x4
    ]
    ;
}

function testElementLiteralWithNamespacesSD() {
      xml x1 = xml `<name1><fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1></name1>`;
  xml x2 = xml `<name2><fname><foo>5</foo><bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2></name2>`;
       xml x3 = x1 + x2 + xml `<foo>apple</foo>`;

       xml x4 =   x3 / * ;
    xml x5 =  x1 / <fname>;
    xml x6 =   x3 / <fname> / <foo> [ 1 ];
    xml x7 =  x3 / < fname > [ 1 ] / < foo > ;
    xml x8 =x3 / *.<bar>[1];
    xml x9 = x3/* /*;

    xmlns "foo" as ns;
    xmlns "bar" as k;
    xml x1 = xml `<ns:root></ns:root>`;
    xml x2 =x1 .<*> ;    // get all elements
    xml x3 =   x1.<ns:*>;
    xml x4 =x1.< ns : root>;
    xml x5 =     x1.<ns : other>;
    xml x6 = x1 . < other > ;
    xml x7 =x1 . <k : *>;

    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
        xml `<root><ns:child>B</ns:child></root>`,
        xml `<item><k:child>C</k:child><child2>D</child2>TEXT</item>`);
    xml x2 =x1 / <child | child2 >;
    xml x3 =  x1/*;
    xml x4 =  x1/< * >;
    xml x5 = x1/ **/ <ns : child | k : child | child2 >;
    xml x6 = x1/ <child | child2 > [ 0 ] ;
    xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;
    int nodeCount =  xdata / * . length( ) ;
    int elementCount =xdata /* . elements( ) . length( ) ;
}