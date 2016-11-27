# Team-Housing project for DMD course

## Team

* Kulaev Oleg
* Kiselev Anton
* Magerramov Emil
* Vinogradov Vladislav

## Project folders structure

* app/
	- assets.javascript/ - Javascript classes and KnockoutViewModels
	- controllers/ - MVC controllers
	- models/ - classes which handle all interaction with DB
	- modules/ - enable specific module for PlayFramework, dooesn't matter
	- utils/ - some helpers
	- views/ - all views except admin, login, index are partial
	- conf/ - application configuration
		* db.migration.default/ - db migrations
		* default_inserts - initial filling of database
		* application.conf - app configuration
		* routes - all web app routes, with specifying corresponding controller
	- libexec/ - some dependencies
	- Content Generation.ipynb - python notebook wich create script that initialize db with some values
	
## How to start project?

    * install [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
    * install [SBT](http://www.scala-sbt.org/download.html)
    * install [Postgresql](https://www.postgresql.org/download/)
    * create database in pg, yo can name it 'team_housing'
    * in `/Windows/System32/drivers/etc/hosts` add line `127.0.0.1   team.housing`
    * in `conf/application.conf` specify database credentials, example:
		- db.default.url="jdbc:postgresql://localhost:5432/team_housing"
		- db.default.username="worker"
		- db.default.password="worker_password123%"
	* in project root folder run coomand `sbt run`
	* access application on team.housing:9000
	* click `Apply migration` in the web app
	* initialize db by running `conf\default_inserts.sql`
	* run `conf\configure_storage[AutoGenerated].sql`