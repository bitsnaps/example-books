package ratpack.examples.book

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.ApplicationUnderTest
import ratpack.test.embed.EmbeddedApp
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.http.client.RequestSpec
import ratpack.test.http.TestHttpClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.IgnoreRest
//import ratpack.test.remote.RemoteControl

import spock.lang.Shared
import spock.lang.Specification

class BookApiSpec extends Specification {

    @AutoCleanup
    @Shared
    ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()

    final static int NBR_BOOKS = 0

   @Shared
   EmbeddedApp isbndb = GroovyEmbeddedApp.of {
       handlers {
           all {
               render '{"data" : [{"title" : "Groovy in Action", "publisher_name" : "Manning Publications", "author_data" : [{"id" : "dierk_koenig", "name" : "Dierk Koenig"}]}]}'
           }
       }
   }

    @Shared
    TestHttpClient client = isbndb.httpClient
//    RemoteControl remote = new RemoteControl(aut)

    def setupSpec() {
//        System.setProperty('eb.isbndb.host', "http://${isbndb.address.host}:${isbndb.address.port}")
//        System.setProperty('eb.isbndb.apikey', "fakeapikey")
    }

    def cleanup() {
//        remote.exec {
//            get(Sql).execute("delete from books")
//        }
    }

    def cleanupSpec() {
//        System.clearProperty('eb.isbndb.host')
    }

    def "list empty books"() {
        given:
            def json = new JsonSlurper()
            def books = json.parseText(client.getText("api/book")) as Map
        expect:
            books != null
            books['data'].size() == 1
            books['data'][0]['title'] == 'Groovy in Action'
    }

    def "create book"() {
        given: 'book information provided'
            def json = new JsonSlurper()
            def requestBody = [isbn: "1932394842", quantity: 10, price: 22.34]
        when: 'Sending a request to create a book'
            def response = aut.httpClient.request("api/book") {
                it.post()
                it.body.type("application/json")
                it.body.text(JsonOutput.toJson(requestBody))
            }

        then: 'that book should be created successfully'
        response.status.code == 200
        def book = json.parseText(response.body.text)
        with(book) {
            isbn == "1932394842"
            title == "Groovy in Action"
            author == "Dierk Koenig"
            publisher == "Manning Publications"
            quantity == 10
            price == 22.34
        }

        when: 'Sending a request to listing books'
            response = aut.httpClient.get("api/book")

        then: 'The created book should be listed.'
            response.status.code == 200
            List books = json.parseText(response.body.text)
            books.size() == NBR_BOOKS + 1
            with(books.last()) {
                isbn == "1932394842"
                title == "Groovy in Action"
                author == "Dierk Koenig"
                publisher == "Manning Publications"
                quantity == 10
                price == 22.34
            }
    }

}
