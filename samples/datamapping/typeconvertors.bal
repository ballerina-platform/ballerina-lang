typeconvertor mapAccount2EnterpriseAccount (xmlElement<t1> in) (xmlElement<t2>) {
  var string name = xml.get(in, "/name", null);
  var xmlElement<smalladdr> addr = mapAddress (xml.get (in, "/address", null));
  var xmlElement out =
    `<EnterpriseAccount>
       <name>$name</name>
       $addr
    </EnterpriseAccount>`;
  return out;
}

typeconvertor mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  var string addrstring = xml.get(in, "/address/number", null) + " " +
                          xml.get(in, "/address/street", null) + ", " +
                          xml.get(in, "/address/city", null);
  return `<address>$addrstring</address>`;
}

typeconvertor map_string_to_int (string s) (int) {
  return s.parseInt();
}

typeconvertor mapPerson2Driver(json<j1> in) (json<j2>){
	var string name = json.get(in, "$.name", null);
	var json out = '{"driver": {"name": "$.name" }}'
    return out;
}


var s = "1234";
int i = s;
