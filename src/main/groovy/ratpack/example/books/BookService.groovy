package ratpack.example.books

import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j
import ratpack.exec.Promise
import javax.inject.Inject

@Slf4j
class BookService {

    private final BookDbCommands bookDbCommands
    private final IsbnDbCommands isbnDbCommands

    @Inject
    BookService(BookDbCommands bookDbCommands, IsbnDbCommands isbnDbCommands) {
        this.bookDbCommands = bookDbCommands
        this.isbnDbCommands = isbnDbCommands
    }

    void createTable() {
        log.info("Creating database tables")
        bookDbCommands.createTables()
    }

    Promise<List<Book>> all() {
        bookDbCommands.all.flatMap { List<GroovyRowResult> rows ->
            Promise.async { def data ->
                if (rows.isEmpty()) {
                    data.success(null)
                }
                List<Book> books = []
                rows.each { GroovyRowResult row ->
                    isbnDbCommands.getBookRequest(row.isbn).then { String jsonResp ->
                        def result = new JsonSlurper().parseText(jsonResp)
                        books << new Book(
                                row.isbn,
                                row.quantity,
                                row.price,
                                result.data[0].title,
                                result.data[0].author_data[0].name,
                                result.data[0].publisher_name
                        )
                        if (books.size() == rows.size()) {
                            data.success(books)
                        }
                    }
                }
            }
        } as Promise
    }

    Promise<String> insert(String isbn, long quantity, BigDecimal price) {
        Promise.async { def data ->
            bookDbCommands
                    .insert(isbn, quantity, price)
                    .map {isbn}
                    .then {
                        data.success(isbn)
                    }
        } as Promise
    }

    Promise<Book> find(String isbn) {
        Promise.async { def data ->
            bookDbCommands.find(isbn).then { GroovyRowResult dbRow ->
                if (dbRow == null) {
                    data.success(null)
                } else {
                    isbnDbCommands.getBookRequest(isbn)
                            .then { String jsonResp ->
                                def result = new JsonSlurper().parseText(jsonResp)
                                data.success(new Book(
                                        isbn,
                                        dbRow.quantity,
                                        dbRow.price,
                                        result.data[0].title,
                                        result.data[0].author_data[0].name,
                                        result.data[0].publisher_name
                                ))
                    }
                }
            }
        } as Promise

    }

    Promise<Void> update(String isbn, long quantity, BigDecimal price) {
        bookDbCommands.update(isbn, quantity, price)
    }

    Promise<Void> delete(String isbn) {
        bookDbCommands.delete(isbn)
    }
}
