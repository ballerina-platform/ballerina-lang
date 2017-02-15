package samples.datamapping;

typemapper mapAccount2EnterpriseAccount (xmlElement<t1> in) (xmlElement<t2>) {
    string name;
    xmlElement<smalladdr> addr;
    xmlElement out;

    name = xml:get(in, "/name", null);
    addr = mapAddress (xml:get (in, "/address", null));
    out = `<EnterpriseAccount>
            <name>$name</name>
                $addr
            </EnterpriseAccount>`;
    return out;
}

typemapper mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
    string addrstring;
    addrstring = xml:get(in, "/address/number", null) + " " +
                          xml:get(in, "/address/street", null) + ", " +
                          xml:get(in, "/address/city", null);
    return `<address>$addrstring</address>`;
}

typemapper map_string_to_int (string s) (int) {
  return string:parseInt(s);
}

typemapper mapPerson2Driver(json<Person> in) (json<Driver>){
    string name;
    json out;
    name = json:get(in, "$.name", null);
    out = `{"driver": {"name": "$.name" }}`;
    return out;
}