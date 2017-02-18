Description
===========
This is a sample Twitter connector with tweet action. And the action is being invoked in a ballerina main function.

Prerequisites
=============
1. Create a twitter app by visiting https://apps.twitter.com/
2. Obtain the following parameters
    * Consumer Key (API Key)
    * Consumer Secret (API Secret)
    * Access Token
    * Access Token Secret

IMPORTANT: This access token can be used to make API requests on your own account's behalf. Do not share your access token secret with anyone.


How to run this sample
======================
bin$ ./ballerina run main ../samples/twitterConnector/twitterConnector.bal <consumerKey> <consumerSecret> <accessToken> <accessTokenSecret> <message>
