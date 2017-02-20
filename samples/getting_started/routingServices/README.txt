Description
===========
This sample has two services which will route the message to two different backend. The first service will route based on the message content and the second one will route
based on the header value.


How to run this sample
======================
bin$ ./ballerina run service ../samples/routingServices/routingServices.bsz

the above command will start the ballerina server in the current terminal and deploy the routingServices.bsz file and publish the  following services.
'contentBasedRouting', 'headerBasedRouting', 'nyseStockQuote' and 'nasdaqStocksQuote'
Here the 'nyseStockQuote' and 'nasdaqStocksQuote' will act as backend.


Invoking the service
====================
curl -v http://localhost:9090/cbr -d '{"name" : "nyse"}'
curl -v http://localhost:9090/cbr -d '{"name" : "nasdaq"}'

curl -v http://localhost:9090/hbr -H "name: nyse"
curl -v http://localhost:9090/hbr -H "name: nasdaq"
