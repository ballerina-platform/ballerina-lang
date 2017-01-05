This sample will invoke medium feeds and get all the @wso2 stories.
And tweet the first item's title.


Pre-request.
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

bin$ ./ballerinaserver.sh ../samples/Sample3/TweetMediumFeed.bal

the above command will start the ballerina server in the current terminal and deploy the TweetMediumFeed.bal file and publish the service 'TweetMediumFeed'.


Invoking the service
====================

curl -v http://localhost:9090/tweet/feed -H"consumerKey:<consumerKey>" -H"consumerSecret:<consumerSecret>" -H"accessToken:<accessToken>" -H"accessTokenSecret:<accessTokenSecret>"
