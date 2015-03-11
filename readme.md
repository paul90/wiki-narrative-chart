# Federated Wiki Narrative Chart


This is a federated wiki add-on is a work in progress that adds a different way of viewing a page's
evolution in context of its twins in a federated wiki neighbourhood.

This view is currently not integrated into either the client, or server. Until it is the following
provides a way of getting an early view of this add-on.



1. Use git to make a local clone of this repository.

1. Open dist/client/index.html in a browser.


The file url will need to have a page name (slug) and list of sites appended, taking the
following form:

`.../dist/client/index.html#`*page-name*`@`*site*

Multiple sites can be added by adding more `@*site*`







This add-on is developed using [ClojureScript](http://clojure.org/clojurescript), with
[OM](https://github.com/swannodette/om) for the UI.
