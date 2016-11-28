package samples.datamapping;

typeconvertor mapRequestToHotelRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string name = xml.get(in, "/name", null);
  xmlElement<smalladdr> addr = mapAddress (xml.get (in, "/address", null));
  xmlElement out =
    `<Hotels>
       <name>$name</name>
       $addr
    </Hotels>`;
  return out;
}

typeconvertor mapAddress (xmlElement<bigaddr> in) (xmlElement<smalladdr>) {
  string addrstring = xml.get(in, "/address/number", null) + " " +
                          xml.get(in, "/address/street", null) + ", " +
                          xml.get(in, "/address/city", null);
  return `<address>$addrstring</address>`;
}

typeconvertor requestToCarRequest (xmlElement<t1> in) (xmlElement<t2>) {
  string category = xml.get(in, "/category", null);
  xmlElement<smallDetails> details = mapDetails (xml.get (in, "/details", null));
  xmlElement out =
    `<Cars>
       <category>$category</category>
       $details
    </Cars>`;
  return out;
}

typeconvertor mapDetails (xmlElement<bigDetails> in) (xmlElement<smallDetails>) {
  string details = xml.get(in, "/order/capacity", null) + " " +
                          xml.get(in, "/order/pickup", null) + ", " +
                          xml.get(in, "/order/destination", null);
  return `<address>$details</address>`;
}
