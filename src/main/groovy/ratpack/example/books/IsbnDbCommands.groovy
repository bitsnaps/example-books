package ratpack.example.books

import com.google.inject.Inject
import ratpack.exec.Promise
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
//import ratpack.core.http.client.HttpClient
//import ratpack.core.http.client.ReceivedResponse

class IsbnDbCommands {

    private final IsbndbConfig config
    private final HttpClient httpClient

    @Inject
    IsbnDbCommands(IsbndbConfig config, HttpClient httpClient) {
        this.config = config
        this.httpClient = httpClient
    }

    Promise<String> getBookRequest(final String isbn) {
        URI uri = "${config.host}/api/v2/json/${config.apikey}/book/$isbn".toURI()
        httpClient.get(uri).map { ReceivedResponse resp ->
            if (resp.body.text.contains("Daily request limit exceeded")) {
                throw new RuntimeException("ISBNDB daily request limit exceeded.")
            }
            resp.body.text
        }
    }

}
