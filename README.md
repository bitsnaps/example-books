example-books
=============

An example Groovy &amp; Gradle based Ratpack app.

This app demonstrates the usage of the following libraries:
* Metrics
* Authentication
* Blocking I/O
* Async external HTTP requests
* ~~RxJava integration (*)~~
* ~~Hystrix integration (*)~~
* ~~WebSockets (*)~~
* Async logging
* External configuration
* Request logging.
(*) These libraries deprecated they'll be  removed in future [releases](https://github.com/ratpack/ratpack/blob/master/release-notes.md).

Setup
-----

This application integrates with [ISBNdb](http://isbndb.com/account/logincreate) and as such you can create an
account and api key in order to run the application successfully [^1].  And at the moment to run the integration tests too.

When you have done this simply add your api key to the property `isbndb.apikey` in the `application.properties` file.

[^1]: Or use the provided [api](./api/README.md) for testing.

Deploy
------

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)
