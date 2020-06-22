function testXMLCommentLiteral() returns [xml, xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33";
 xml x1  = xml     `<!--aaa-->`   ;xml x2  =  xml`<!--${ v1  }-->`  ;
        xml  x3   =xml   `<!--aaa${   v1}bbb${v2   }ccc-->`  ;
    xml x4 =    xml   `<!--<aaa${  v1  }bbb${  v2}cccd->e->-f<<{>>>-->`  ;
    xml x5 =   xml`<!---a-aa${ v1 }b\${bb${ v2      }c\}cc{d{}e}{f{-->`   ;
    xml x6 =xml`<!---->`   ;
    xml x7 =
                 xml     `<!-- $ -->`;
    return [x1, x2, x3, x4, x5, x6];
}

function testXMLCommentLiteral1() returns [xml, xml, xml, xml, xml, xml] {
    int v1 = 11;
    string v2 = "22";
    string v3 = "33"; xml    x1    =
         xml      `<!--aaa-->`    ;
    xml    x2 =    xml     `<!--${v1}-->`   ;
    xml    x3   =
        xml `<!--aaa${
 v1
         }bbb${
           v2}ccc-->`
         ;xml x4 =     xml`<!--<aaa${
        v1
 }bbb${
        v2
           }cccd->e->-f<<{>>>-->`
    ;
    xml x5 =
    xml       `<!---a-aa${
       v1     }b\${bb${
       v2     }c\}cc{d{}e}{f{-->`     ;
 xml x6 =
 xml `<!---->`
        ;xml x7=
                     xml`<!-- $ -->`;
    return [x1, x2, x3, x4, x5, x6];
}