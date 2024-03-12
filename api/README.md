# ISBN API

This is Mock API for testing purposes:
You can use it with [json-server@v0.17.4](https://www.npmjs.com/package/json-server) by running: `json-server -w api/db.json --routes api/routes.json --host 127.0.0.1` or using this [Ratpack](../server.groovy) script by running: `groovy server.groovy` using Groovy v3.0.x.


## REST API Spec:
```bash
# Get all books
curl -X GET http://localhost:3000/books

# Get book by ID
curl -X GET http://localhost:3000/api/v2/json/book/1617293022
```
