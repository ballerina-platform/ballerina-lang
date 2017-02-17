package samples.datamapping;

typemapper mapAccount2EnterpriseAccount (xmlElement<t1> in) (xmlElement<t2>) {
    string name;
    xmlElement<smalladdr> addr;
    xmlElement out;

    name = xmlutils:get(in, "/name", null);
    addr = mapAddress (xmlutils:get (in, "/address", null));
    out = `<EnterpriseAccount>
            <name>$name</name>
                $addr
            </EnterpriseAccount>`;
    return out;
}

typemapper mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
    string addrstring;
    addrstring = xmlutils:get(in, "/address/number", null) + " " +
                          xmlutils:get(in, "/address/street", null) + ", " +
                          xmlutils:get(in, "/address/city", null);
    return `<address>$addrstring</address>`;
}

typemapper map_string_to_int (string s) (int) {
  return strings:parseInt(s);
}

typemapper mapPerson2Driver(json<Person> in) (json<Driver>){
    string name;
    json out;
    name = jsonutils:get(in, "$.name", null);
    out = `{"driver": {"name": "$.name" }}`;
    return out;
}