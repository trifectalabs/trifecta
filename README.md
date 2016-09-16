#Trifecta

Trifecta is a web application which allows athletes to generate training plans fit to their fitness level (calculated from historic data pulled from Strava), schedule these training plans with their Google calendar, and have routes around their location suggested for each activity in the training plan.

Trifecta consists of several microservices which work together to deliver a full application experience. These are:

	- Osprey: Central API responsible for main database interaction and periodic jobs
	- Raven: Training plan generation.
	- Peacock: Front-end web application. Data is sourced via JSON from Osprey. Was written in Elm; rewritten in JS.
	- Arctic-tern: Route generation service.
	- Social-weaver: Training plan scheduling service. Integrates with Google Calendar.
	- Condor: Infrastructure tooling. Cloudformation templates, Docker images, etc.

Trifecta was created as a fourth-year engineering project for the University of Waterloo. This project now has been open-sourced for documentation purposes - if the code here helps you in any way then it has fufilled its purpose. It is not expected to be run in whole without modification/configuring.
