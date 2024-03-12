import com.zaxxer.hikari.HikariConfig
import ratpack.example.books.Book
import ratpack.example.books.BookModule
import ratpack.example.books.BookRestEndpoint
import ratpack.example.books.BookService
import ratpack.example.books.DatabaseHealthCheck
import ratpack.example.books.ErrorHandler
import ratpack.example.books.IsbndbConfig
import ratpack.example.books.MarkupTemplateRenderableDecorator
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator

//import org.pac4j.http.client.indirect.FormClient
//import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.dropwizard.metrics.MetricsWebsocketBroadcastHandler
import ratpack.dropwizard.metrics.DropwizardMetricsConfig
import ratpack.dropwizard.metrics.DropwizardMetricsModule
import ratpack.error.ServerErrorHandler
import ratpack.form.Form
import ratpack.groovy.sql.SqlModule
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.health.HealthCheckHandler // Deprecated in v2.0 use: ratpack.core.health
import ratpack.hikari.HikariModule
//import ratpack.hystrix.HystrixModule
import ratpack.pac4j.RatpackPac4j
//import ratpack.rx2.RxRatpack
import ratpack.service.Service
import ratpack.service.StartEvent
import ratpack.session.SessionModule
import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

final Logger logger = LoggerFactory.getLogger(ratpack.class)
//final def json = { Object o -> JsonOutput.toJson(o) }

ratpack {

    serverConfig {
        port(5050)
        props("application.properties")
        sysProps("eb.")
        env("EB_")
        require("/isbndb", IsbndbConfig)
        require("/metrics", DropwizardMetricsConfig)
    }

    bindings {

        module new DropwizardMetricsModule(), { DropwizardMetricsConfig config ->
            config.jmx()//.console()

        }
        bind DatabaseHealthCheck
        module HikariModule, { HikariConfig c ->
            c.addDataSourceProperty("URL", "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV")
            c.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource")
        }
        module SqlModule
        module BookModule
        module SessionModule
        module MarkupTemplateModule
//        module new HystrixModule().sse()
        bind MarkupTemplateRenderableDecorator

        bindInstance Service, new Service() {
            @Override
            void onStart(StartEvent event) throws Exception {
//                logger.info("Initializing RX")
//                RxRatpack.initialize()
                event.registry.get(BookService).createTable()
            }
        }

        bind ServerErrorHandler, ErrorHandler
    }

  handlers { BookService bookService ->

      get() {
          bookService.all()
            .then { List<Book> books ->
                String isbndbApikey = context.get(IsbndbConfig).apikey
                render groovyMarkupTemplate("listing.gtpl",
                        isbndbApikey: isbndbApikey,
                        title: "Books",
                        books: books,
                        msg: request.queryParams.msg ?: "")
          }
      } // path: '/'

      path("book") {
          byMethod {
              get {
                  bookService.find(request.queryParams['isbn']).then { def book ->
                      if (book) {
                      render groovyMarkupTemplate("show.gtpl",
                              title: "Book Information",
                              isbn: book.isbn,
                              quantity: book.quantity,
                              price: book.price,
                              buttonText: ''
                      )
                      } else {
                          render groovyMarkupTemplate("show.gtpl",
                                  title: "Book Information",
                                  msg: 'Cannot find Book ISBN',
                                  buttonText: ''
                          )
                      }
                  }
              }
          }
      } // path: /book

      path("create") {
          byMethod {
              get {
                  render groovyMarkupTemplate("create.gtpl",
                          title: "Create Book",
                          isbn: '',
                          quantity: '',
                          price: '',
                          method: 'post',
                          action: '',
                          buttonText: 'Create'
                  )
              }
              post {
                  parse(Form).then
                            { Form form ->
                                  bookService.insert(
                                          form.isbn,
                                          form.get("quantity").asType(Long),
                                          form.get("price").asType(BigDecimal)
                                  ).then({ def isbn ->
                                      redirect "/?msg=Book+$isbn+created"
                                  })
                            }
              }
          }
      } // path: /create

      path("update/:isbn") {
          def isbn = pathTokens["isbn"]
          bookService.find(isbn).then { Book book ->
              if (book == null) {
                  clientError(404)
              } else {
                  byMethod {
                      get {
                          render groovyMarkupTemplate("update.gtpl",
                                  title: "Update Book",
                                  method: 'post',
                                  action: '',
                                  buttonText: 'Update',
                                  isbn: book.isbn,
                                  bookTitle: book.title,
                                  author: book.author,
                                  publisher: book.publisher,
                                  quantity: book.quantity,
                                  price: book.price)
                      }
                      post {
                          parse(Form).
                                  flatMap { Form form ->
                                      bookService.update(
                                              isbn,
                                              form.get("quantity").asType(Long),
                                              form.get("price").asType(BigDecimal)
                                      )
                                  }.
                                  then {
                                      redirect "/?msg=Book+$isbn+updated"
                                  }
                      }
                  }
              }
          }
      } // path: /update/:isbn

      post("delete/:isbn") {
          def isbn = pathTokens["isbn"]
          bookService.delete(isbn).
              then {
                  redirect "/?msg=Book+$isbn+deleted"
              }
      } // path: /delete/:isbn

      prefix("api/book") {
          all chain(registry.get(BookRestEndpoint))
      } // prefix: api/book

      def pac4jCallbackPath = "pac4j-callback"
      all(RatpackPac4j.authenticator(
              pac4jCallbackPath,
              new FormClient("/login", new SimpleTestUsernamePasswordAuthenticator())))

      prefix("admin") {
          all(RatpackPac4j.requireAuth(FormClient.class))

          get("health-check/:name?", new HealthCheckHandler())
          get("metrics-report", new MetricsWebsocketBroadcastHandler())

          get("metrics") { def ctx ->
              render groovyMarkupTemplate("metrics.gtpl", title: "Metrics")
          }
      }
//      get("hystrix.stream", new HystrixMetricsEventStreamHandler())

      get("login") { def ctx ->
          render groovyMarkupTemplate("login.gtpl",
                  title: "Login",
                  action: "/$pac4jCallbackPath",
                  method: 'get',
                  buttonText: 'Login',
                  error: request.queryParams.error ?: "")
      }

      get("logout") { def ctx ->
          RatpackPac4j.logout(ctx).then {
              redirect("/")
          }
      }

      files { it.dir("public") }

      get('docs') {
          redirect('/docs/index.html')
      }

  }
}
