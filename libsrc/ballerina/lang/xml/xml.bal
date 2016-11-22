package ballerina.lang.xml;

native function get(xmlElement e, string xPath, map namespaces) (xmlElement);
native function set(xmlElement e, string xPath, map namespaces, xmlElement value);

native function get(xmlElement e, string xPath, map namespaces) (string);
native function set(xmlElement e, string xPath, map namespaces, string value);

native function remove(xmlElement e, string xPath, map namespaces);

//todo: add other functions (append, insertBefore, insertAfter, rename)