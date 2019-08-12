#!/bin/sh

set -e

apk update && apk add --no-cache git
sbt test
