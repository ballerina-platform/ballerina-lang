import ballerina/grpc;


configurable int port = 9090;

type Book record {
    string id;
    string title;
    string author;
    float price;
};

Book[] books = [
        {id: "1", title: "Book One", author: "Author One", price: 29.99},
        {id: "2", title: "Book Two", author: "Author Two", price: 19.99},
        {id: "3", title: "Book Three", author: "Author Three", price: 39.99}
    ];

service "Books" on new grpc:Listener(port)  {
    remote function  addBook(Book book) returns Book | error {
        books.push(book);
        return book;
    }
    remote  function getBook(string id) returns Book | error {
        Book[] book = books.filter(b => b.id == id);
        if (book.length() == 0) {
            return error grpc:NotFoundError(string `Cannot find the album for ID ${id}`);
        }
        return book[0];
    }



    remote  function listBooks( ) returns  stream<Book, error?> | error {
        return books.toStream();
    }
}
