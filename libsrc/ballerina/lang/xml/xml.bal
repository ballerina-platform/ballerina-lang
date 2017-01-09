package ballerina.lang.xml;

native function getXml(xml e, string xPath) (xml);
native function getXml(xml e, string xPath, map namespaces) (xml);

native function getString(xml e, string xPath) (string);
native function getString(xml e, string xPath, map namespaces) (string);

native function set(xml e, string xPath, xml value);
native function set(xml e, string xPath, xml value, map namespaces);

native function set(xml e, string xPath, string value);
native function set(xml e, string xPath, string value, map namespaces);

native function remove(xml e, string xPath);
native function remove(xml e, string xPath, map namespaces);

native function addElement(xml e, string xPath);
native function addElement(xml e, string xPath, map namespaces);

native function addAttribute(xml e, string xPath);
native function addAttribute(xml e, string xPath, map namespaces);

native function toString(xml e) (string);
