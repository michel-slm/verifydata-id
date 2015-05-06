# verifydata-id

[![Build Status](https://travis-ci.org/michel-slm/verifydata-id.svg)](https://travis-ci.org/michel-slm/verifydata-id)

A Clojure web service for verifying identities against various
Indonesian governmental data sources. Currently implements identity
card verification against the data held by the electoral commission.

## Usage

* Install [Leiningen](https://github.com/technomancy/leiningen#installation)
* lein ring server
* curl -i http://localhost:3000/verify-nik/:nik
* curl -i http://localhost:3000/verify-nik-name/:nik/:name

## License

Copyright © 2015 Michel Alexandre Salim

Distributed under the Apache Software License, either version 2.0 or (at
your option) any later version.
