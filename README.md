# Document Library Common Components

Common utilities and components used in the document library including:

* Custom BSON codecs for converting Scala objects to BSON structures - `io.mdcatapult.doclib.codec`
* Custom Json converters for converting json to Scala types - `io.mdcatapult.doclib.json`
* Common message structures - `io.mdcatapult.doclib.messages`
* Common object models - `io.mdcatapult.doclib.models`
* DocLibFlags - Object to allow easy checking and manipulation of doclib flags ie started, ended, errored - `io.mdcatapult.doclib.models.DocLibFlag`  
* TargetPath - Trait to include tooling for the generation of doclib target paths for a source document - `io.mdcatapult.doclib.path.TargetPath`
* PrefetchUtils - Trait with various convenience methods used by the `Prefetch` process for a `DocLibDoc` - `io.mdcatapult.doclib.util.PrefetchUtils`.

## Creating a new Consumer
The basic pattern is that the `Consumer` creates the queues and connections to Mongo. Then it creates a handler for the messages.
The `Consumer` should extend the `AbstractConsumer` class.
### Abstract Handler
Handlers within a consumer should extend the `AbstractHandler` class with the appropriate typed parameters that represent the type of message `Envelope` received and
the `HandlerResult` that is returned. The handle method accepts a `CommittableReadResult` which contains the rabbit message
that extends the Envelope trait and returns a tuple which contains the original message and a `HandlerResult`. The `handle` method represents the 
"business logic" that a Queue is subscribed with.
The postHandleProcess receives messages that extend the HandlerResult trait, ensuring a message's associated DoclibDoc
has the correct post processing operations applied. This includes logging, optionally sending a message to the supervisor,
and writing appropriate flags to the DoclibDoc.


## Testing
To run tests, do
```bash
sbt clean test
```
For integration tests, do
```bash
docker-compose up -d
sbt clean it:test
```
To do all tests and get a coverage report, do
```bash
docker-compose up -d
sbt clean coverage test it:test coverageReport
```

## Dependency Scanning

https://github.com/albuch/sbt-dependency-check

The sbt-dependency-check plugin can be used to create a HTML report under `target/scala-x.x/dependency-check-report.html`

```bash
sbt dependencyCheck
```

## Dependency Issues

Tika > 1.28.5 causes compilation issues
```bash
[error] While parsing annotations in /Users/ian.dunlop/.ivy2/cache/org.mongodb/mongodb-driver-core/jars/mongodb-driver-core-4.4.1.jar(com/mongodb/lang/Nullable.class), could not find MAYBE in enum <none>.
[error] This is likely due to an implementation restriction: an annotation argument cannot refer to a member of the annotated class (scala/bug#7014).
```

Prometheus > 0.9.0 causes integration test failures in `ConsumerHandlerSpec`.