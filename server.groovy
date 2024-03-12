@Grapes([
  @Grab("io.ratpack:ratpack-groovy:2.0.0-rc-1"),
  @Grab("org.slf4j:slf4j-simple:1.7.36")
])

import groovy.json.JsonSlurper
import static ratpack.groovy.Groovy.ratpack
import ratpack.core.http.Status
//import static ratpack.jackson.Jackson.json // old API
import static ratpack.core.jackson.Jackson.json

def jsonFile = new File('./api/db.json')
def jsonSlurper = new JsonSlurper()
def ebooks = jsonSlurper.parse(jsonFile)['books']

def findBookById = { def ctx ->
    def id = ctx.pathTokens.id
    def ebook = ebooks.find { it.isbn == id }
    if (ebook) {
        ctx.render json(ebook)
    } else {
        ctx.response.status(404).send("Ebook with ID $id not found")
    }
}

ratpack {
    serverConfig {
        port(3000)
    }
    handlers {
        get() {
            redirect('/books')
        }
        get('books') {
            render json(ebooks)
        }
        get('books/:id') { def ctx ->
            findBookById(ctx)
        }
        get('api/v2/json/book/:id'){ def ctx ->
            findBookById(ctx)
        }
        get('api/v2/json/:apikey/book/:isbn'){
            render json(ebooks.find { it.isbn == pathTokens.isbn })
        }
    }
}
