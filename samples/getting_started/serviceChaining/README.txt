Description
===========
This sample will illustrate a composite service : ATMLocatorService, which calls the following services to find the nearest ATM of ABC Bank using the zip code.

- Zip-code based Branch locator service : http://localhost:9090/branchlocator
- Bank information provider : http://localhost:9090/bankinfo



How to run this sample
======================
bin$ ./ballerina run service ../samples/serviceChaining/ATMLocatorService.bsz

This starts the ATMLocatorService as well as the two backend services required to run the sample.

Invoking the service
====================
Request : curl -X POST -d '{"ATMLocator": {"ZipCode": "95999"}}' -v "http://localhost:9090/ABCBank/locator"



