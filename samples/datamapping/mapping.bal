function foo (message m) {
  // suppose I know that the message payload is XML and its schema type:
  xmlElement<{http://example.com/xsd/SalesForce}Account> inXML =
     message.getPayload (m);

// UC 1
  // what I want to end up is with an XML Element who's schema type is
  // EnterpriseAccount.
  xmlElement<{http://example.com/xsd/SalesForce}EnterpriseAccount> outXML;

  // I have to write the mapping function and register
  // it in the "system". After that the following assignment will work:
  outXML = inXML;

// UC 2
  // Produce a JSON document who's type is "account"
  json<"account"> outJson;

  // assuming a mapper has been registered then following will work:
  outJson = (TheOtherType)((MyType)inXML);
}
