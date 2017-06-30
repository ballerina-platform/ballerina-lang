Description
===========
This sample will invoke gitHub api and get all the open pull request for a given repo.
And tweet the total number of open pull request against the given repo.


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
bin$ ./ballerina run main ../samples/tweetOpenPR/tweetOpenPR.bal <consumerKey> <consumerSecret> <accessToken> <accessTokenSecret> [<repo-name>]

Here the repo-name is optional, if it is not specified, wso2-synapse will be the default repo-name.
