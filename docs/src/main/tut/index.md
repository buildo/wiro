---
layout: home
title:  "Home"
section: "home"
---

[![Download](https://api.bintray.com/packages/buildo/maven/wiro-http-server/images/download.svg)](https://bintray.com/buildo/maven/wiro-http-server/_latestVersion)
[![Build Status](https://drone.our.buildo.io/api/badges/buildo/wiro/status.svg)](https://drone.our.buildo.io/buildo/wiro)

<a name="getting-started"></a>

{% include_relative getting-started.md %}

## Features

- Automatic generation of http routes from controllers
- Extensible error module

## What's wrong with routers?

We think routes should be a one-to-one mapping with controllers' methods.
Wiro exposes controllers' operations using HTTP as a transport protocol.

This is sometimes referred to as *WYGOPIAO*: What You GET Or POST Is An Operation, and it's closely related to RPC.


{% include_relative step-by-step-example.md %}
