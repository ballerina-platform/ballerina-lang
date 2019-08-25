xmlns "http://ballerina.com/b" as ns1;

function testElementLiteralWithNamespaces() returns [xml, xml] {
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/a" as ns0;
    xmlns "http://ballerina.com/c" as ns1;

 xml    x1=xml    `<   root ns0 : id = "456" >< foo >123</ foo >< bar ns1 : status  =  "complete" ></ bar ></ root >` ;xml x2  =  x1  .  *  ;
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
        .
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
    xml    x4=    x3 . * ;
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
    .
    *
    ;
    return [
    x3
    ,
    x4
    ]
    ;
}