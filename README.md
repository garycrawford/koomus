[![Build Status](https://travis-ci.org/garycrawford/koomus.svg?branch=master)](https://travis-ci.org/garycrawford/koomus)
[![Build Status](https://snap-ci.com/garycrawford/koomus/branch/master/build_image)](https://snap-ci.com/garycrawford/koomus/branch/master)

# bulk-loader

## Usage

### Start the system
#### From REPL
1. Start repl: `lein repl`
2. Move to user namespace: `(ns user)`
3. Start component system: `(reset)`

#### As jar
1. Generate uberjar with lein: `lein uberjar`
2. Locate standalone jar: `ls target`
3. Launch jar: `java -Dhost=localhost -Dport=1234 -jar target/bulk-loader-0.1.0-SNAPSHOT-standalone.jar`

### Run tests
* To run all tests: `lein midje`
* To avoid slow tests: `lein midje :filter -slow`

### Process Dicom
1. Start system: `From REPL` or `As jar` above
2. Send image to system with: `curl http://localhost:1234/api/images?path={path to dicom directory}`

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
