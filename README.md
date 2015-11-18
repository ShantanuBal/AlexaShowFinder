#Alexa Skills Kit for searching events and cab booking

##Background
Amazon's Echo is a device powered by a cloud based voice recognition system called Alexa. (https://en.wikipedia.org/wiki/Amazon_Echo)
Each app that is built for this device is called a skill. During development this skill is available only for the developer.

##Functionality
1. We use the Ticketmaster's APIs to search for events happening at a location on a certain date.
2. We use the APIs from Uber to query price and time estimates and also book an Uber.

##Architecture
1. What we speak to Alexa is published to our service hosted on Amazon Lambda.
2. This lamda service calls either the Ticketmaster API or the Uber API as per the user's input.
3. There is no other requirement for the Ticketmaster API.
4. For Uber we need an oath2.0 token to make a Uber request.
   * The lamda function queries the DynamoDb hosted on AWS for the access key for the current user.
   * If this is present then an Uber request is made using this access key.
   * If the auth token is not present or has expired a request is made to to receive the code to generate the access key.
   * Uber then verifies the user and make a GET request to the Django SSL end point we have setup in Microsft Azure.
   * This end point upon Uber's request updates the DynamoDb with the user name and the auth token, and continues to make the uber request.

##Project setup and Usage
1. clone this project and run a `mvn clean` followed by `mvn assembly:assembly -DdescriptorId=jar-with-dependencies package`. 
2. The target folder will contain `alexa-skills-kit-samples-1.1-jar-with-dependencies.jar`. Upload this to AWS Lambda as per their instructions (https://github.com/pramodsetlur/alexa-skill-kit/tree/master/src/main/java/helloworld).
3. The Django SSL endpoint is present at https://github.com/pramodsetlur/UberAlexa-Azure-Endpoint (currently made a private repository as per Amazon's request. We plan to make this public again after we the hide sensitive information.). This has to be hosted with an open port on 443 (an https end point).

##Various uttarances you can make to Amazon echo after a successful setup
1. Start show finder
    Response - welcome to show finder. Tell me a location and a date to search for events.
2. Events in Los Angeles on Novemeber 15. 
    Response - Black Sabbath, event 2, event 3, Do you want to hear a few more?
3. Tell me more about Event 1
    Response - Black Sabbath is starting at 10:00PM at Rodeo Drive
4. Give me Uber details about event 1
    Response - Estimated time for an Uber is 2 minutes. It is 14 miles to the destination. The price estimates is betweenn $12 to $20.
5. Book an uber to Event 1
    Response - Your Uber has been scheduled
