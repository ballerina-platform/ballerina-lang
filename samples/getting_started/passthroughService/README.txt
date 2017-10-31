Description
===========
This sample will passthrough the message to the backend and response to the client


How to run this sample
======================
bin$ ./ballerina run ../samples/passthroughService/passthroughService.balx

The above command will start the 'passthroughService' and 'nyseStockQuoteService' services in the 'passthroughService.balx' file.
Here the 'nyseStockQuoteService' will act as a backend.

Invoking the service
====================
curl -v http://localhost:9090/passthrough
