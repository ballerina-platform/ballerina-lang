import ballerina.lang.xml;
import ballerina.lang.system;

function main (string[] args) {

    xml xml1;
    xml xml2;
    xml xml3;
    xml xml4;
    xml message;

    string name;

    xml1 = `<persons><person><name>Jack</name><address>wso2</address></person></persons>`;
    xml2 = `<person><name>Jack</name></person>`;
    xml3 = `<name><fname>Jack</fname><lname>Peter</lname></name>`;
    xml4 = `<address>wso2</address>`;

    //set string to a xml.
    xml:set(xml1, "/persons/person/name/text()", "Peter");
    system:println(xml:toString(xml1));
    //set xml to a xml.
    xml:set(xml2, "/person/name", xml3);
    system:println(xml:toString(xml2));

    //get a string from a xml.
    system:println(xml:getString(xml1, "/persons/person/name/text()"));
    //get a xml from a xml.
    system:println(xml:toString(xml:getXml(xml1, "/persons/person")));

    //add an element to a xml.
    xml:addElement(xml2, "/person", xml4);
    system:println(xml:toString(xml2));

    //add attribute to a xml.
    xml:addAttribute(xml2, "/person/name", "id", "person123");
    system:println(xml:toString(xml2));

    //remove an element from a xml.
    xml:remove(xml1, "/persons/person/address");
    system:println(xml:toString(xml1));

    //accessing template variables.
    name = "Jack";
    message = `<name>${name}</name>`;
    system:println(xml:toString(message));

    //converting an xml to string.
    system:println(xml:toString(xml1));
}

