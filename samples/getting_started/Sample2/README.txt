Description
===========
This sample will invoke gitHub api and get all the open pull request for a given (if not given, wso2-synapse) repo.
And tweet the total number of open pull request against the given repo.


Prerequisites
===========
1. Create a twitter app by visiting https://apps.twitter.com/
2. Obtain the following parameters
    * Consumer Key (API Key)
    * Consumer Secret (API Secret)
    * Access Token
    * Access Token Secret

IMPORTANT: This access token can be used to make API requests on your own account's behalf. Do not share your access token secret with anyone.


How to run this sample
======================

bin$ ./ballerinaserver.sh ../samples/Sample2/TweetOpenPR.bal

the above command will start the ballerina server in the current terminal and deploy the TweetOpenPR.bal file and publish the service 'TweetOpenPR'.


Invoking the service
====================

curl -v http://localhost:9090/tweet/pr?repo=product-esb -H"consumerKey:<consumerKey>" -H"consumerSecret:<consumerSecret>" -H"accessToken:<accessToken>" -H"accessTokenSecret:<accessTokenSecret>"

Here the query parameter 'repo' is optional, if we don't specify the query parameter 'wso2-synapse' will get queried for open pull request.