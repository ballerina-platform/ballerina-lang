package ballerina.lang.xml;

native function getXml(xml e, string xPath, map namespaces) (xml);
native function getString(xml e, string xPath, map namespaces) (string);

native function set(xml e, string xPath, map namespaces, xml value);
native function set(xml e, string xPath, map namespaces, string value);

native function remove(xml e, string xPath, map namespaces);

native function toString(xml e) (string);

native function addElement(xml e, string xPath, map namespaces);
native function addAttribute(xml e, string xPath, map namespaces);