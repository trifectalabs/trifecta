![Osprey](https://cloud.githubusercontent.com/assets/4472397/6365683/d2e4e4c8-bc89-11e4-8afd-12da522611f3.png)
Osprey
=======

Central Trifecta core service and API.

[API Documentation](http://www.apidoc.me/trifectalabs.com/osprey/latest)

[ ![Codeship Status for trifectalabs/osprey](https://codeship.com/projects/f5d78e90-b08c-0132-6835-3a7a9fb44a4e/status?branch=master)](https://codeship.com/projects/69590)

#####Responsibilities:

- Data import from Strava
- Match and bet creation
- Generate Cumulative Activities
- API for all core models
- Job System
	- Close expired matches and bets
	- Poll for new Activities from Strava
	- Close Ranking periods


Dev Environment Setup
=====================

#####OS X Setup (with Homebrew and SBT)
-----
######Setup Postres
	brew install postgresql
	initdb /usr/local/var/postgres -E utf8
	createuser trifecta_admin -S
	createdb trifecta_web -U trifecta_admin

#####Update Local Schema
**This will wipe your existing DB**

	> From the Osprey git directory
	dropdb trifecta_web && createdb trifecta_web
	pg_restore -h localhost --clean -C -O -d trifecta_web -U trifecta_admin db.tar

#####Modify DB Schema
1) Make sure you have the latest schema locally (see above)

2) Make changes to your local DB as needed (using PSQL, PGAdmin, etc)

3) When ready to test new schema, run...

	pg_dump -h localhost -C -O -n trifecta -s -f db.tar -F tar trifecta_web

3) Run test_setup.sh to update the test db with the new schema.

4) Test

4) When finished, add `db.tar` in your commit along with the respective model changes as needed. 


Run/Deploy
===========

#####Locally
-------
With Postgres running,

	sbt run

To run with the Job system, edit `conf/application.conf` and change

	job.service.bool = false
to

	job.service.bool = true
and then run.

Osprey runs on port 9000

#####Staging
-------
Script coming soon

#####Production
-------
Script coming soon
