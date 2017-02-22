package samples.datamapping;

typemapper mapRequestToHotelRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string name;
  xmlElement<smalladdr> addr;
  xmlElement out;

  name = xmls:get(in, "/name", nil);
  addr = mapAddress (xmls:get (in, "/address", nil));
  out =
    `<Hotels>
       <name>$name</name>
       $addr
    </Hotels>`;
  return out;
}

typemapper mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  string addrstring;
  addrstring = xmls:get(in, "/address/number", nil) + " " +
                          xmls:get(in, "/address/street", nil) + ", " +
                          xmls:get(in, "/address/city", nil);
  return `<address>$addrstring</address>`;
}

typemapper requestToCarRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string category;
  xmlElement<smallDetails> details;
  xmlElement out;

  category = xmls:get(in, "/category", nil);
  details = mapDetails (xmls:get (in, "/details", nil));
  out =
    `<Cars>
       <category>$category</category>
       $details
    </Cars>`;
  return out;
}

typemapper mapDetails (xmlElement<bigDetails> in) (xmlElement<smallDetails>) {
  string details;
  details = xmls:get(in, "/order/capacity", nil) + " " +
                          xmls:get(in, "/order/pickup", nil) + ", " +
                          xmls:get(in, "/order/destination", nil);
  return `<address>$details</address>`;
}
