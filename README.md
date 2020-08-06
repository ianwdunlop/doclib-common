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

* **DoclibFlag** - Object to allow easy checking and manipulatoin of v2 flags
* **MongoCodecs** - Simple generation of Mongo Codecs based on models in the common library
* **TargetPath** - Trait to include tooling for the generation of target paths that intersect with the source path

# Testing
To run tests, do
```bash
sbt +clean +test
```
For integration tests, do
```bash
docker-compose up -d
MONGO_HOST=localhost sbt +clean +it:test
```
To do all tests and get a coverage report, do
```bash
docker-compose up -d
MONGO_HOST=localhost sbt +clean coverage +test +it:test coverageReport
```
