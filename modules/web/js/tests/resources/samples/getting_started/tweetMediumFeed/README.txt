Description
===========
This sample will invoke medium feeds and get all the @wso2 stories.
And tweet the first item's title.


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
bin$ ./ballerina run main ../samples/tweetMediumFeed/tweetMediumFeed.bal <consumerKey> <consumerSecret> <accessToken> <accessTokenSecret>
