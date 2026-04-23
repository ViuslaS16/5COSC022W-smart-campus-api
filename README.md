# Smart Campus Sensor & Room Management API (5COSC022W)

## 1. Overview of API Design
This project implements a RESTful web service for a Smart Campus environment using JAX-RS (Jakarta EE 10) with an embedded Tomcat HTTP server. The API exposes `Room` and `Sensor` models, allowing clients to manage physical spaces and ingest environmental sensor readings into an ephemeral, in-memory data store. The architecture adopts the Sub-Resource Locator pattern to model parent-child relationships (Resources > Readings), guaranteeing a clean URL structure (`/api/v1/sensors/{id}/readings`). Comprehensive domain-specific exception handling has been baked into the REST layer through standard Provider mappers to return precise HTTP status codes (201, 204, 404, 409, 422, 403, 500) and structured JSON error responses. Concurrency checks are inherently provided through backing `ConcurrentHashMap` and thread-safe collections avoiding data corruption across request threads.

## 2. Build and Run Instructions

**Prerequisites:** 
- Java 17+
- Apache Maven 3.6+

**Build App:**
```bash
mvn clean package
```

**Run Server:**
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

The embedded web server will start running automatically and expose endpoints strictly at `http://localhost:8080/api/v1`. By default, the application registers the `JacksonFeature` for deep JSON serialization bindings.

## 3. Five cURL Examples

1. **Discovery Endpoint**
```bash
curl -X GET http://localhost:8080/api/v1
```

2. **Create Room**
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

3. **Get Room**
```bash
curl -i -X GET http://localhost:8080/api/v1/rooms/LIB-301
```

4. **Create Sensor**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"LIB-301"}'
```

5. **Submit Sensor Reading (Sub-Resource)**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d '{"value":22.5}'
```

## 4. Report Questions

---

### Q1: JAX-RS lifecycle per-request vs singleton and impact on ConcurrentHashMap synchronization

> By default, JAX-RS instantiates a new resource class object per incoming HTTP request to guarantee thread safety inside the controller bounds. Since every request spins up its own object reference and execution thread, sharing data across the entire application requires singletons or static variables. We leverage `ConcurrentHashMap` because standard maps (`HashMap`) are inherently unsafe under multiprocessor concurrent reads/writes and will throw `ConcurrentModificationException` or corrupt internal bucket linkings. The concurrent map relies on segmented locks or comparison-and-swap algorithms to ensure that multiple default-scoped JAX-RS requests can read and mutate the simulated database safely simultaneously.

---

### Q2: Why HATEOAS in discovery endpoint benefits clients vs static docs

> HATEOAS (Hypermedia As The Engine Of Application State) drastically decouples the client from hardcoded endpoint URIs by supplying navigable links directly inside the HTTP payload. Instead of a client consulting a static PDF to form the string `/api/v1/rooms`, it dynamically inspects the discovery endpoint's tree returning dynamic references. This allows the backend developers to modify routing configurations entirely unilaterally without breaking legacy consuming applications, as clients dynamically discover where features live. Ultimately, this leads to a more resilient, self-documenting service architecture that reflects the true intent of REST over basic RPC calls.

---

### Q3: Implications of returning IDs only vs full objects for GET /rooms (bandwidth, client processing)

> Returning extensive, deeply nested lists inside a parent `Room` model incurs severe network bandwidth penalties and memory bloat when scaling the number of sensors. Conversely, fetching merely the primitive scalar properties and providing an array of associated `Sensor` IDs mitigates massive N+1 serialization depths at the REST boundary. While returning IDs is incredibly lightweight on the wire, it shifts the operational burden to the client which must then issue sequential or parallel HTTP GET requests to resolve the individual nested resources. This paradigm aligns gracefully with mobile device patterns needing fast initial renders but forces engineers to carefully weigh round-trip latencies against serialization bottlenecks.

---

### Q4: Is DELETE idempotent, justify with repeated calls

> Idempotency dictates that executing a specific operation multiple times generates the identical secondary state and semantic effect as performing it once. Initially, calling `DELETE /api/v1/rooms/LIB-301` removes the entity safely from the database and yields a `204 No Content` to affirm deletion success. If the exact same DELETE request is sequentially executed against the now-missing entity, the server purposefully intercepts the null lookup gracefully and continuously returns the exact same `204 No Content` code. This defensive behavior strictly adheres to REST principles by avoiding `404 Not Found` upon subsequent calls, guaranteeing the ultimate state remains identically "entity absent" without generating superfluous error noise.

---

### Q5: What JAX-RS returns when @Consumes JSON receives text/plain (415 Unsupported Media Type)

> When a client specifies an invalid data envelope—such as sending a raw string under `text/plain` to a handler strictly requesting `@Consumes(MediaType.APPLICATION_JSON)`—the JAX-RS provider immediately filters the request. Because the container physically lacks a compatible `MessageBodyReader` capable of unmarshalling primitive strings into Jackson-annotated DTOs, the framework fundamentally rejects the handoff. It intercepts execution natively to short-circuit the controller, directly returning an HTTP `415 Unsupported Media Type` to inform the caller that their formatting is incompatible. This declarative pattern prevents arbitrary payload structures from poisoning business controller semantics.

---

### Q6: Why @QueryParam is superior to path param for filtering

> Addressing a collection via `@PathParam` conventionally dictates looking up a distinct explicit resource hierarchy (e.g., `/sensors/TEMP-001` locates one discrete object). Conversely, modifying a generalized collection response based on arbitrary constraints heavily warrants the use of `@QueryParam` parameters to maintain semantic purity (e.g., `/sensors?type=Temperature`). Using path semantics for filtering violates REST principles because filtering criteria aren't structural entities — they are transient adjustments to query representations. Query semantics gracefully scale to accommodate optional, combinable, and multi-faceted parameters, avoiding an exponential explosion of fragile path configurations.

---

### Q7: Architectural benefits of Sub-Resource Locator

> Implementing a Sub-Resource Locator simplifies controller fragmentation by delegating requests cleanly down a semantic tree node, exactly like a classic URL directory. Returning a dynamically initialized class (e.g., `SensorReadingResource`) intrinsically bonds the context of the parent (the instantiated sensor ID) to the child boundary context. This isolates the `Readings` maintenance logic comprehensively outside the `SensorResource`, preventing bloated controllers that typically suffer from hundreds of disjoint HTTP handler methods. Such delegation explicitly enforces Single Responsibility modularity while naturally maintaining clean path routing hierarchies.

---

### Q8: Why 422 is more accurate than 404 for missing roomId in payload

> A generic HTTP `404 Not Found` typically implies the actual request URI itself does not map to a configured valid endpoint definition or an expected parent lookup. However, when evaluating a `POST /sensors` instruction, the HTTP routing engine validates that the entity target URL definitively exists and is contactable. The actual failure occurs internally because the supplied JSON body fundamentally violates business validation (an unresolvable `roomId`), rendering the syntax mathematically valid but experientially unprocessable. Consequently, returning `422 Unprocessable Entity` correctly signals that the route exists perfectly, but the semantic instruction provided within the domain bounds is fatally unusable.

---

### Q9: Cybersecurity risks of exposing stack traces (list info attacker gains)

> Blindly dispatching raw internal runtime exception stack traces to unauthenticated external consumers is considered a severe application security vulnerability. Such detailed traces expose invaluable reconnaissance intelligence, explicitly revealing underlying execution framework topologies, specific transitive library versions, and precise internal routing paths. Malevolent actors can harvest this dense topological fingerprinting map to surgically exploit documented CVE attack vectors associated with the revealed dependency artifacts. Masking these internal failures exclusively behind an opaque `500 Internal Server Error` wrapper effectively sanitizes the error response and shields the structural implementation against deliberate exploitation.

---

### Q10: Why filters are better than manual logging in each method

> Injecting explicit `.log(...)` invocations into every single HTTP worker method systematically destroys codebase purity by violating the core "Don't Repeat Yourself" (DRY) paradigm. This inevitably introduces chronic human error risks where subsequent developers forget to prepend or append tracking logic inside newly crafted downstream handlers. JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` leverage inversion-of-control aspect-oriented concepts, intercepting the HTTP lifecycle totally orthogonally to controller bounds. This guarantees immutable, zero-friction, 100% universal logging across all legacy and future endpoint surfaces, profoundly enforcing observability from a centralized configuration source.
