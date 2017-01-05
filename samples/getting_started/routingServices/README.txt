Description
===========
This sample has two services which will route the message to two different backend. The first service will route based on the message content and the second one will route
based on the header value.


How to run this sample
======================
bin$ ./ballerinaserver.sh ../samples/routingServices/routingServices.bal

the above command will start the ballerina server in the current terminal and deploy the routingServices.bal file and publish the  following services.
'contentBasedRouting', 'headerBasedRouting', 'nyseStockQuote' and 'nasdaqStocksQuote'
Here the 'nyseStockQuote' and  will act as a backend.