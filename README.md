[![Build Status](https://travis-ci.org/garycrawford/koomus.svg?branch=master)](https://travis-ci.org/garycrawford/koomus)
[![Build Status](https://snap-ci.com/garycrawford/koomus/branch/master/build_image)](https://snap-ci.com/garycrawford/koomus/branch/master)

# bulk-loader

## Usage

N.B. this is nothing more than a spike atm - please do not use!

Use `lein run` to launch the app using the hardcoded image path. This will create and populate a directory called `store` with Neo4j data. By adjusting the neo4j property `org.neo4j.server.database.location` in Neo4j's `conf/neo4j-server.properties` file to point to this directory the image data will be loaded into Neo4j with the normal start process.

*N.B. `allow_store_upgrade=true` must be set in `conf/neo4j.properties` at the moment due to a versioning problem to be resolved.*

I will pull this together into a shell script for launching some point soon.

To run the tests, use `lein midje`

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
