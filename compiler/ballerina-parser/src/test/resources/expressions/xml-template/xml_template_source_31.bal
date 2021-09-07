function foo() {
    xml a = xml `<![CDATA[some text]]>`;

    xml b = xml `<![CDATA[]]>`;

    xml c = xml `XML stands for <![CDATA[eXtensible]]> Markup Language`;

    xml d = xml `<![CDATA[{int[3] y = <int[3]> w;}]]>`;

    xml e = xml `<element>some text<![CDATA[some other text]]></element>`;

    xml f = xml `<![CDATA[value of variable a is --> ${a}]]>`;

    xml g = xml `<![CDATA[${expression1 + expression2} is the sum]]>`;

    xml h = xml `<![CDATA[abcd ${expression1 + expression2} is the sum ${var2} some text]]>`;

    xml i = xml `<![CDATA[>>
                hellow!]]>`;

    xml j = xml `<![CDATA[<greeting>Hello, world!</greeting>]]>`;
}
