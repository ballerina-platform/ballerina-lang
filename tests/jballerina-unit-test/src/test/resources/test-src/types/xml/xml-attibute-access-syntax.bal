
function getElementAttrBasic() returns string|error {
    xml x = xml `<root attr="attr-val"><a></a><b></b></root>`;
    string|error val = x.attr;
    return val;
}


function getAttrOfASequance() returns string|error {
    xml x = xml `<root attr="attr-val"><a attr="a-attr"></a><b attr="b-attr"></b></root>`;
    string|error name = x/*.attr; // get children sequences' attribute `attr`
    return name;
}
