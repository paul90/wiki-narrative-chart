# Federated Wiki Narrative Chart

__*This project is a work in progress*__


This is a federated wiki add-on is a work in progress that adds a different way of viewing a page's
evolution in context of its twins in a federated wiki neighbourhood.

This view is currently not available as a package, or integrated into either the client, or server.
Until it there it is possible to run the latest version by using git to make a local clone of this
repository, and  open dist/client/index.html in a browser.


The file url will need to have a page name (slug) and list of sites appended, taking the
following form:

```
.../dist/client/index.html#<page-name>@<site>
```

Where `<page-name>` is the slug of the page you want to look at, and `<site>` the domain to site
to load the page from. Multiple sites can be added by appending more `@<site>`.


There is a pair of wiki pages that provide [further information](http://fedwiki.rodwell.me/view/visualizing-page-history/view/narrative-chart).


This add-on is developed using [ClojureScript](http://clojure.org/clojurescript), with
[OM](https://github.com/swannodette/om).
