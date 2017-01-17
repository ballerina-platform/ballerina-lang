import ballerina.lang.xml;
import ballerina.lang.system;

function main (string[] args) {

    xml xml1;
    xml xml2;
    xml xml3;
    xml xml4;

    xml1 = `<persons><person><name>Jack</name><address>wso2</address></person></persons>`;
    xml2 = `<person><name>Jack</name></person>`;
    xml3 = `<name><fname>Jack</fname><lname>Peter</lname></name>`;
    xml4 = `<address>wso2</address>`;

    //set string to a xml.
    xml:set(xml1, "/persons/person/name/text()", "Peter");
    //set xml to a xml.
    xml:set(xml2, "/person/name", xml3);

    //get a string from a xml.
    xml:getString(xml1, "/persons/person/name/text()");
    //get a xml from a xml.
    xml:getXml(xml1, "/persons/person");

    //add an element to a xml.
    xml:addElement(xml2, "/person", xml4);

    //add attribute to a xml.
    xml:addAttribute(xml2, "/person/name", "id", "person123");

    //remove an element from a xml.
    xml:remove(xml1, "/persons/person/address");

    //converting an xml to string.
    system:println(xml:toString(xml1));
}