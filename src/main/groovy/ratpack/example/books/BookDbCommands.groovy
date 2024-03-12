package ratpack.example.books

import com.google.inject.Inject
//import com.netflix.hystrix.HystrixCommandGroupKey
//import com.netflix.hystrix.HystrixCommandKey
//import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.Blocking
import ratpack.exec.Promise
//import static ratpack.rx2.RxRatpack.observe
//import static ratpack.rx.RxRatpack.observeEach

class BookDbCommands {

    private final Sql sql

    @Inject
    BookDbCommands(Sql sql) {
        this.sql = sql
    }

    void createTables() {
        sql.executeInsert("drop table if exists books")
        sql.executeInsert("create table books (isbn varchar(13) primary key, quantity int, price numeric(15, 2))")
        // You can insert some books as examples, but don't forget to update `NBR_BOOKS` at the testing files.
//        sql.executeInsert("insert into books (isbn, quantity, price) values ('1617293022', 10, 22.34)")
//        sql.executeInsert("insert into books (isbn, quantity, price) values ('0134685997', 5, 17.54)")
    }

    Promise<GroovyRowResult> getAll() {
        Blocking.get {
            sql.rows("select isbn, quantity, price from books order by isbn")
        } as Promise
    }

     Promise<String> insert(final String isbn, final long quantity, final BigDecimal price) {
        Blocking.get{
            try {
                sql.executeInsert("insert into books (isbn, quantity, price) values ($isbn, $quantity, $price)")
            } catch (def e){
                println("ERROR: ${e}")
            }
        } as Promise
    }

    Promise<GroovyRowResult> find(final String isbn) {
        Blocking.get {
            sql.firstRow("select quantity, price from books where isbn = $isbn")
        }
    }

    Promise<Void> update(final String isbn, final long quantity, final BigDecimal price) {
        Blocking.get {
            sql.executeUpdate("update books set quantity = $quantity, price = $price where isbn = $isbn")
        } as Promise
    }

    Promise<Void> delete(final String isbn) {
        Blocking.get {
            sql.executeUpdate("delete from books where isbn = $isbn")
        } as Promise
    }

}
