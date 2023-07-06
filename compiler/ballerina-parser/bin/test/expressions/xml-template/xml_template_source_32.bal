function foo() {
    xml a = xml `<![CDATA[first text ]]> second text ]]>`;

    xml b = xml `<![CDATA[first text `;

    xml c = xml `<<![CDATA[>]]>> xml content <<![CDATA[>]]>/>`;

    xml d = xml `]]>`;

    xml e = xml `<ns:a><![CDATA[first text <a/>`;
}
