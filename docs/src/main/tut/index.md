---
layout: home
title:  "Home"
section: "home"
---

[![Download](https://api.bintray.com/packages/buildo/maven/wiro-http-server/images/download.svg)](https://bintray.com/buildo/maven/wiro-http-server/_latestVersion)
[![Build Status](https://drone.our.buildo.io/api/badges/buildo/wiro/status.svg)](https://drone.our.buildo.io/buildo/wiro)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/53a17fb6396c4a0daa835c407ca22866)](https://www.codacy.com/app/claudio_4/wiro?utm_source=github.com&utm_medium=referral&utm_content=buildo/wiro&utm_campaign=badger)

<a name="getting-started"></a>

{% include_relative getting-started.md %}

## Why Wiro?

Wiro is a lightweight Scala library to automatically generate http routes from scala traits.
At buildo, we don't like writing routes. Writing routes is an error-prone and frustrating procedure. Futhermore, in most of our use-cases, it can be completely automatized.

## Features

Here is the list of the most relevant features of Wiro:

- Automatic generation of http routes from decorated Scala traits
- Automatic generation of an http client that matches the generated routes
- Extensible error module (based on type classes)
- Custom routes definition (in case you want to use http methods or write paths that Wiro doesn't provide)
- Support for HTTP authorization header

{% include_relative step-by-step-example.md %}

{% include_relative authorization.md %}
