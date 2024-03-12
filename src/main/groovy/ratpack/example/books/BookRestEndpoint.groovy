package ratpack.example.books

import com.fasterxml.jackson.databind.JsonNode
import ratpack.groovy.handling.GroovyChainAction
import javax.inject.Inject
//import static ratpack.core.jackson.Jackson.json
//import static ratpack.core.jackson.Jackson.jsonNode
import static ratpack.jackson.Jackson.json
import static ratpack.jackson.Jackson.jsonNode
//import static ratpack.rx2.RxRatpack.observe

class BookRestEndpoint extends GroovyChainAction {

    private final BookService bookService

    @Inject
    BookRestEndpoint(BookService bookService) {
        this.bookService = bookService
    }

    @Override
    void execute() throws Exception {
        path(":isbn") {
            String isbn = pathTokens["isbn"]

            byMethod {
                get {
                    bookService.find(isbn).then { Book book ->
                        if (book == null) {
                            clientError 404
                        } else {
                            render book
                        }
                    }
                }
                put {
                    parse(jsonNode()).flatMap { JsonNode input ->
                        bookService.update(
                                isbn,
                                input.get("quantity").asLong(),
                                input.get("price").asDouble() as BigDecimal
                        ).flatMap {
                            bookService.find(isbn)
                        }
                    }.then { Book book ->
                        render book
                    }
                }
                delete {
                    bookService.delete(isbn).then {
                        response.send()
                    }
                }
            }
        }

        all {
            byMethod {
                get {
                    bookService.all().then { List<Book> books ->
                        render json(books)
                    }
                }
                post {
                    parse(jsonNode()).flatMap { JsonNode input ->
                        bookService.insert(
                                input.get("isbn").asText(),
                                input.get("quantity").asLong(),
                                input.get("price").asDouble() as BigDecimal
                        ).flatMap {
                            bookService.find(it.toString())
                        }
                    }.then { Book createdBook ->
                        render createdBook
                    }
                }
            }
        }
    }
}
