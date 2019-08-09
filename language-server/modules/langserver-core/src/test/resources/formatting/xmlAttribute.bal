function testExpressionAsAttributeValue() returns [xml, xml, xml, xml, xml] {
    string v0 = "\"zzz\"";
    string v1 = "zzz";
    string v2 = "33>22";
    xml  x1=xml    `<foo  bar   = "${  v0 }"/>`   ;xml x2 = xml     `< foo    bar  ="aaa${  v1}bb'b${v2   }ccc?" />` ;
          xml x3=xml`< foo bar = "}aaa${   v1   }bbb${    v2   }ccc{d{}e}{f{" />`   ;
  xml x4=   xml    `< foo bar1 = 'aaa{${ v1}}b\${b"b${v2}c\}cc{d{}e}{f{'    bar2 =  'aaa{${ v1  }}b\${b"b${ v2 }c\}cc{d{}e}{f{'/>` ;
       xml    x5=   xml`< foo   bar = "" />` ;
    return [x1, x2, x3, x4, x5];
}

function testExpressionAsAttributeValue1() returns [xml, xml, xml, xml, xml] {
    string v0 = "\"zzz\"";
    string v1 = "zzz";
    string v2 = "33>22";
    xml x1 =
    xml`<
 foo
        bar
    =
        "${
 v0
       }"/>`
  ;
    xml  x2  =
       xml  `<
  foo
         bar
 =
    "aaa${
          v1
 }bb'b${
        v2
        }ccc?"/>`
      ;
    xml x3   =
xml     `<
  foo
       bar
    =
       "}aaa${
    v1
 }}bbb${
          v2
         }ccc{d{}e}{f{"/>`
         ;
    xml   x4=
           xml `<foo
 bar1
           =   'aaa{${ v1
          }}b\${b"b${
         v2
 }c\}cc{d{}e}{f{' bar2='aaa{${
 v1
       }}b\${b"b${
 v2
        }c\}cc{d{}e}{f{'/>`
        ;
    xml    x5 =    xml`<   foo
 bar    =
         ""/>`
    ;
    return [x1, x2, x3, x4, x5];
}

function testDefineInlineNamespace() returns (xml) {
    xml x1 =   xml`< foo  foo = "http://wso2.com"  >hello</ foo >` ;
    return x1;
}

function testDefineInlineNamespace1() returns (xml) {
    xml x1 =
    xml `<
 foo
          foo
        =
       "http://wso2.com"
    >hello</
         foo>`
 ;
        return x1;
}