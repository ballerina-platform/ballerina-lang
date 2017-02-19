package sample.message_transformation.data_mapping;

function foo (message m) {
  // following examples assume that type mappers are regsitered, and please see typemapper.bal for an example.
  // suppose I know that the message payload is XML and its schema type:
   xml<{"http://example.com/xsd/SalesForce"}Account> inXML;
   xml<{"http://example.com/xsd/SalesForce"}EnterpriseAccount> outXML;
   json<{"Accounts"}account> outJson;


    inXML = messages:getPayload(m);
// UC 1
  // what I want to end up is with an XML Element who's schema type is
  // EnterpriseAccount.
  // I have to write the mapping function and register
  // it in the "system". After that the following assignment will work:
  outXML = inXML;

// UC 2
  // Produce a JSON document who's type is "account"

  // assuming a mapper has been registered then following will work:
  outJson = (TheOtherType)((MyType)inXML);
}