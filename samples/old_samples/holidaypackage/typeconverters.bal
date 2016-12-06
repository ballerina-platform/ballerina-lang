package samples.datamapping;

typeconvertor mapRequestToHotelRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string name = xml:get(in, "/name", nil);
  xmlElement<smalladdr> addr = mapAddress (xml:get (in, "/address", nil));
  xmlElement out =
    `<Hotels>
       <name>$name</name>
       $addr
    </Hotels>`;
  return out;
}

typeconvertor mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  string addrstring = xml:get(in, "/address/number", nil) + " " +
                          xml:get(in, "/address/street", nil) + ", " +
                          xml:get(in, "/address/city", nil);
  return `<address>$addrstring</address>`;
}

typeconvertor requestToCarRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string category = xml:get(in, "/category", nil);
  xmlElement<smallDetails> details = mapDetails (xml:get (in, "/details", nil));
  xmlElement out =
    `<Cars>
       <category>$category</category>
       $details
    </Cars>`;
  return out;
}

typeconvertor mapDetails (xmlElement<bigDetails> in) (xmlElement<smallDetails>) {
  string details = xml:get(in, "/order/capacity", nil) + " " +
                          xml:get(in, "/order/pickup", nil) + ", " +
                          xml:get(in, "/order/destination", nil);
  return `<address>$details</address>`;
}
