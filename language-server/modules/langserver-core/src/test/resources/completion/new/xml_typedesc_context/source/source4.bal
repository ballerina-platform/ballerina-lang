import ballerina/lang.'xml;

type NEW_XML_TYPE 'xml:Element;

function name() {
    xml<'xml:X> elementSequence = xml `<hello>xml content</hello>`;
    int testVar = 12;
}
