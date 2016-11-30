package samples.datamapping;

typeconvertor mapAccount2EnterpriseAccount (xmlElement<t1> in) (xmlElement<t2>) {
  string name = xml:get(in, "/name", null);
  xmlElement<smalladdr> addr = mapAddress (xml.get (in, "/address", null));
  xmlElement out =
    `<EnterpriseAccount>
       <name>$name</name>
       $addr
    </EnterpriseAccount>`;
  return out;
}

typeconvertor mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  string addrstring = xml:get(in, "/address/number", null) + " " +
                          xml:get(in, "/address/street", null) + ", " +
                          xml:get(in, "/address/city", null);
  return `<address>$addrstring</address>`;
}

typeconvertor map_string_to_int (string s) (int) {
  return s.parseInt();
}

typeconvertor mapPerson2Driver(json<Person> in) (json<Driver>){
    string name = json:get(in, "$.name", null);
    json out = '{"driver": {"name": "$.name" }}'
    return out;
}
