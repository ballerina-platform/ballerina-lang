function foo() {
    xml a = xml `</body>`;
    xml b = xml `<foo></bar></foo>`;
    xml c = xml `<foo> hello </bar></foo>`;
    xml d = xml `<foo/></bar>`;
    xml e = xml `<ns0:foo> hello </baz><bar/></ns0:foo>`;

    xml f = xml `<<`;
    xml g = xml `<<foo>>`;
    xml h = xml `<foo></foo><<`;
    xml k = xml `<<foo></foo>></<foo></foo>>`;
}
