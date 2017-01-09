package samples.datamapping;

typeconvertor mapRequestToHotelRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string name;
  xmlElement<smalladdr> addr;
  xmlElement out;

  name = xml:get(in, "/name", nil);
  addr = mapAddress (xml:get (in, "/address", nil));
  out =
    `<Hotels>
       <name>$name</name>
       $addr
    </Hotels>`;
  return out;
}

typeconvertor mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  string addrstring;
  addrstring = xml:get(in, "/address/number", nil) + " " +
                          xml:get(in, "/address/street", nil) + ", " +
                          xml:get(in, "/address/city", nil);
  return `<address>$addrstring</address>`;
}

typeconvertor requestToCarRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string category;
  xmlElement<smallDetails> details;
  xmlElement out;

  category = xml:get(in, "/category", nil);
  details = mapDetails (xml:get (in, "/details", nil));
  out =
    `<Cars>
       <category>$category</category>
       $details
    </Cars>`;
  return out;
}

typeconvertor mapDetails (xmlElement<bigDetails> in) (xmlElement<smallDetails>) {
  string details;
  details = xml:get(in, "/order/capacity", nil) + " " +
                          xml:get(in, "/order/pickup", nil) + ", " +
                          xml:get(in, "/order/destination", nil);
  return `<address>$details</address>`;
}
