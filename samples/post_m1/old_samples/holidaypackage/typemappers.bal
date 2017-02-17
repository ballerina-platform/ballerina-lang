package samples.datamapping;

typemapper mapRequestToHotelRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string name;
  xmlElement<smalladdr> addr;
  xmlElement out;

  name = xmlutils:get(in, "/name", nil);
  addr = mapAddress (xmlutils:get (in, "/address", nil));
  out =
    `<Hotels>
       <name>$name</name>
       $addr
    </Hotels>`;
  return out;
}

typemapper mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  string addrstring;
  addrstring = xmlutils:get(in, "/address/number", nil) + " " +
                          xmlutils:get(in, "/address/street", nil) + ", " +
                          xmlutils:get(in, "/address/city", nil);
  return `<address>$addrstring</address>`;
}

typemapper requestToCarRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string category;
  xmlElement<smallDetails> details;
  xmlElement out;

  category = xmlutils:get(in, "/category", nil);
  details = mapDetails (xmlutils:get (in, "/details", nil));
  out =
    `<Cars>
       <category>$category</category>
       $details
    </Cars>`;
  return out;
}

typemapper mapDetails (xmlElement<bigDetails> in) (xmlElement<smallDetails>) {
  string details;
  details = xmlutils:get(in, "/order/capacity", nil) + " " +
                          xmlutils:get(in, "/order/pickup", nil) + ", " +
                          xmlutils:get(in, "/order/destination", nil);
  return `<address>$details</address>`;
}
