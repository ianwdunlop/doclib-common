# Document Library Common Components

Common utilities and components used in the document library

## Bson
Custom BSON codecs for converting Scala objects to BSON structures

## Json
Custom Json converters for converting json to Scala types

## Legacy (deprecated)
v1 components

## Loader
file loading utilities (SourceLoader)

## Messages
Common message structures

## Models
Common object models

## Util
Common utility classes

* **DoclibFlag** - Object to allow easy checking and manipulation of v2 flags
* **MongoCodecs** - Simple generation of Mongo Codecs based on models in the common library
* **TargetPath** - Trait to include tooling for the generation of target paths that intersect with the source path

# Consumer

**Abstract Handler**
To be used by a doclib consumer's handler class. The handle method accepts a rabbit message that extends the Envelope trait.
The postHandleProcess receives messages that extend the HandlerResult trait, ensuring a message's associated DoclibDoc
has the correct post processing operations applied. This includes logging, optionally sending a message to the supervisor,
and writing appropriate flags to the DoclibDoc.


# Testing
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
