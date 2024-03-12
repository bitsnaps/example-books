# This Mocks ISBNdb's API, it requires json-server CLI to be installed (npm install -g json-server)
json-server -w api/db.json --routes api/routes.json --host 127.0.0.1

# Some usage examples:
# Get all [10] books:
# curl http://localhost:3000/
# Get 3rd book by ID:
# curl http://localhost:3000/books/3
# Get 4th book by ID:
# curl http://localhost:3000/api/v2/json/book/4
# Get 5th book by isbn number (must provide a [fake] apikey):
# curl http://localhost:3000/api/v2/json/YOUR_API_KEY/book/0132350882

