# HTTP Server (Java)

A multithreaded HTTP/1.1 server built from scratch in Java with zero third-party runtime dependencies. Implements
request parsing, content negotiation, static file serving, form handling with multipart file uploads, session
management, and concurrent request processing — all against the [HTTP/1.1 RFC](https://tools.ietf.org/html/rfc7230).

## Design

### Architecture

The server follows a **thread-per-request** model with a pluggable route handler architecture:

```
Client -> ServerSocket -> HttpRequestParser -> RequestDispatcher -> Route handler -> OutputStream
                                                    |
                                          Folder / File (fallback)
```

**Key abstractions:**

- **`Route`** — Interface with a single method: `serve(ConnectionData, OutputStream)`. All route handlers implement
  this.
- **`ConnectionData`** — Record bundling the parsed `HttpRequest` with the server's root directory.
- **`HttpRequest`** — Immutable record holding method, path, query string, headers, and raw body bytes.
- **`RequestDispatcher`** — Maps the first path segment to a registered `Route`, falling back to `Folder` or `File`
  handlers for static content. Validates path safety to prevent directory traversal.

### Route Handlers

| Route       | Handler   | Description                                                                         |
|-------------|-----------|-------------------------------------------------------------------------------------|
| `/form`     | `Form`    | Renders an HTML form, echoes GET query params, parses multipart POST uploads        |
| `/guess`    | `Guess`   | Number-guessing game with server-side sessions and HttpOnly cookies                 |
| `/hello`    | `Hello`   | Serves the root `index.html`                                                        |
| `/listing`  | `Listing` | Generates HTML directory listings with navigation links                             |
| `/ping`     | `Ping`    | Responds after a configurable delay (seconds in path); used for concurrency testing |
| `*` (files) | `File`    | Serves static files with correct `Content-Type` (HTML, JPEG, PNG, PDF, etc.)        |
| `*` (dirs)  | `Folder`  | Serves `index.html` from directories, or generates a listing if none exists         |

### Session Management

The guessing game demonstrates secure session handling:

- Server-side session store (`GameSession`) backed by `ConcurrentHashMap`
- UUID-based session IDs sent via `Set-Cookie: session=...; HttpOnly`
- Game state tracked entirely server-side in `SessionData` records
- No client-side secrets — the target number lives only on the server

## Prerequisites

- **Java 21+**
- **Maven 3.9+**
- **[Leiningen](https://leiningen.org/)** (only for running the Clojure integration test suite)

## Build and Run

```bash
# Build JAR
mvn package

# Run the server (default: port 80, serving from testroot/)
java -jar target/http-server-java-1.0-SNAPSHOT.jar

# Run with custom port and root directory
java -jar target/http-server-java-1.0-SNAPSHOT.jar -p 8080 -r /path/to/webroot
```

### Command-Line Options

| Flag        | Description                     | Default |
|-------------|---------------------------------|---------|
| `-p <port>` | Port to listen on               | `80`    |
| `-r <dir>`  | Root directory for static files | `testroot` |
| `-h`        | Print help and exit             |         |
| `-x`        | Print startup config and exit   |         |

## Testing

The project has two complementary test suites:

### Unit Tests (JUnit 5)

Each test constructs an `HttpRequest`, passes it to a handler with a `ByteArrayOutputStream`, and asserts on the raw HTTP response.

```bash
mvn test
```

**104 tests** covering: request parsing, URL decoding, path dispatch, path traversal protection, static file serving, directory
listings, form GET/POST handling, session-based game logic, content-type negotiation, CLI argument parsing, and response
formatting.

### Integration Tests (Clojure/speclj)

Black-box tests that start the server as a subprocess and make real HTTP requests. These validate end-to-end behavior
including socket handling, concurrency, and the full request/response cycle.

```bash
# Requires Leiningen and a prior mvn package
mvn package && lein spec
```

**23 specs** covering: command-line interface, basic HTTP responses, file serving with content types, form submission with file upload,
and concurrent request handling.

### Test Configuration

The integration test suite reads `spec/http_spec/config.edn` to locate the server binary:

```edn
{
 :cmd "java -cp target/classes com.alexrootroatch.httpserver.Main"
 :name "My Server"
 :startup-millis 3000
 :debug? false
}
```

## Using as a Library

You can use this server as a dependency in your own project. Add the JAR to your classpath, implement the `Route`
interface, and start the server with custom routes:

```java
import com.alexrootroatch.httpserver.MyServer;
import com.alexrootroatch.httpserver.Route;

import java.util.HashMap;
import java.util.Map;

Map<String, Route> routes = new HashMap<>();
routes.put("/api", new MyApiRoute());
MyServer server = new MyServer(8080, "/var/www", routes);
server.start();
```

The `Route` interface has a single method to implement:

```java
public class MyApiRoute implements Route {
    public void serve(ConnectionData connData, OutputStream outputStream) {
        // Read connData.request() for method, path, headers, body
        // Write HTTP response bytes to outputStream
    }
}
```

## Project Structure

```
src/main/java/com/alexrootroatch/httpserver/
    Main.java              # Entry point, CLI parsing, route registration
    MyServer.java          # ServerSocket listener, thread-per-request model
    HttpRequestParser.java # Parses raw HTTP from InputStream, decodes percent-encoded paths
    HttpRequest.java       # Immutable request record
    ConnectionData.java    # Request + root directory bundle
    RequestDispatcher.java # Route matching, path safety, fallback to File/Folder
    Route.java             # Handler interface
    Print.java             # Help and config output
    routes/
      StaticFile.java      # Static file serving
      Folder.java          # Directory index/listing
      Form.java            # GET/POST form handling, multipart parsing
      Guess.java           # Number guessing game
      GameSession.java     # Thread-safe session store
      Hello.java           # Index page
      Listing.java         # Directory listing with navigation
      Ping.java            # Delayed response for concurrency testing
      DirectoryContents.java # Directory enumeration utility
      HttpResponse.java    # HTTP response writing (200, 404, 405, 500)
      ContentType.java     # MIME type mapping
      HtmlUtil.java        # HTML escaping

src/test/java/com/alexrootroatch/httpserver/
                           # JUnit 5 unit tests
spec/http_spec/            # Clojure integration specs

testroot/                  # Static files served during tests
root/                      # Default root directory of the server
```

## Contributing

### Constraints

This project follows a **no-third-party-libraries** rule for production code. The only external dependency is JUnit
Jupiter for testing. All HTTP parsing, routing, session management, and response formatting is implemented from scratch.

### Adding a Route

1. Create a class in `src/main/java/com/alexrootroatch/httpserver/routes/` implementing `Route`:

    ```java
    public class MyRoute implements Route {
      public void serve(ConnectionData connData, OutputStream outputStream) {
        // Build response, write to outputStream
      }
    }
    ```

2. Register it in `Main.java`:

    ```java
    routes.put("/myroute", new MyRoute());
    ```

3. Add unit tests in `src/test/java/com/alexrootroatch/httpserver/` that call `serve()` directly with a `ByteArrayOutputStream`.

### Running the Full Suite

```bash
mvn package && lein spec
```
