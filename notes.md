# Developer Notes

## Prerequisites

* Java - JDK version 6 or later
* [Light Table](http://www.lighttable.com/) - code editor
* [Leiningen](http://leiningen.org/) - project automation

You may also want the Google Chrome browser, with the 'React Developer Tools' extension.

## Initial Project Creation

A initial leiningen user profile is created in `~/.lein/profiles.clj` to add [lein-ancient](https://github.com/xsc/lein-ancient).

```
{:user {:plugins [[lein-ancient "0.5.5"]]}}
```


The initial state of the project was created using the mies-om leiningen template:

```
lein new mies-om wiki-explorer
```

## Development Process

1. We run `lein cljsbuild auto wiki-explorer` - this will auto rebuild the project when we save a file, or alternatively use [Cuttle](https://github.com/oakmac/cuttle). N.B. The first time may take a little while as the environment gets loaded.

1. Open the project in Light Table, and add an *browser (external)* connection. You will need to edit index-dev.html to replace *xxxx* with the correct port `<script type='text/javascript' id='lt_ws' src='http://localhost:xxxx/socket.io/lighttable/ws.js'></script>` **N.B.** the port number changes each time.

1. We open index-dev.html with a browser (Google Chrome is recommended, but any that has support for source maps is good).







