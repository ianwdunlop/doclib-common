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
Handlers within a consumer should extend the `AbstractHandler` class. The handle method accepts a rabbit message that extends the Envelope trait.
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
