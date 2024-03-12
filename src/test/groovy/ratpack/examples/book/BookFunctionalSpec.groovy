package ratpack.examples.book

import geb.spock.GebReportingSpec
import ratpack.examples.book.pages.BooksPage
import ratpack.examples.book.pages.CreateBookPage
import ratpack.examples.book.pages.UpdateBookPage
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.test.ApplicationUnderTest
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class BookFunctionalSpec extends GebReportingSpec {

    @Shared
    ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()

    // @Shared
    // EmbeddedApp isbndb = GroovyEmbeddedApp.of {
    //     handlers {
    //         all {
    //             render '{"data" : [{"title" : "Jurassic Park: A Novel", "publisher_name" : "Ballantine Books", "author_data" : [{"id" : "cm", "name" : "Crichton, Michael"}]}]}'
    //         }
    //     }
    // }

    final static int NBR_BOOKS = 0

    def setupSpec() {
        // println("URL: http://${isbndb.address.host}:${isbndb.address.port}")
        // System.setProperty('eb.isbndb.host', "http://${isbndb.address.host}:${isbndb.address.port}")
        // System.setProperty('eb.isbndb.apikey', "fakeapikey")
    }

    def setup() {
        browser.baseUrl = aut.address
    }

    def cleanupSpec() {
//        RemoteControl remote = new RemoteControl(aut)
//        remote.exec {
//            get(Sql).execute("delete from books")
//        }
        // System.clearProperty('eb.isbndb.host')
    }

    def "books are listed"() {
        when:
        to BooksPage

        then:
        at BooksPage
        books.size() == NBR_BOOKS
    }


    def "go to create book page"() {
        when:
        createBookButton.click()

        then:
        at CreateBookPage
    }

    def "create book"() {
        when:
        isbnField = "0345538986"
        quantityField = "10"
        priceField = "10.23"
        createButton.click()

        then:
        at BooksPage

        and:
        books.size() == NBR_BOOKS + 1
        with (books.find { it.isbn=='0345538986'} ) {
          title == "Jurassic Park: A Novel"
          author == "Crichton, Michael"
          publisher == "Ballantine Books"
          price == "10.23"
          quantity == "10"
      }
    }

    def "update book"() {
        when:
        books.find { it.isbn=='0345538986'}.updateButton.click()

        then:
        at UpdateBookPage

        when:
        quantityField = "2"
        priceField = "5.34"
        updateButton.click()

        then:
        at BooksPage

        and:
        books.size() == NBR_BOOKS + 1
        with (books.find { it.isbn=='0345538986'}){
          title == "Jurassic Park: A Novel"
          author == "Crichton, Michael"
          publisher == "Ballantine Books"
          price == "5.34"
          quantity == "2"
        }
    }

    def "delete book"() {
        when:
        books.find { it.isbn=='0345538986'}.deleteButton.click()

        then:
        at BooksPage

        and:
        books.size() == NBR_BOOKS
    }

}
