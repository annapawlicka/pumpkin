# pumpkin

Deployed to heroku: [pumpkin](http://evening-citadel-3933.herokuapp.com/).

A sample dashboard demo written with
[Sente](https://github.com/ptaoussanis/sente),
[Om](https://github.com/swannodette/om), and data visualisation
libraries [D3.js](http://d3js.org/) and
[dimple.js](http://dimplejs.org/).

It demonstrates how to connect Om and Sente to push new data to our
client and how to create data vis components and make them interact with
each other. Data visualised comes from GitHub stats of an open source
project - no authorisation neccessary.

It's built on top of an awesome leiningen
template [chestnut](https://github.com/plexus/chestnut) - a real life saver!


## Development

Start a REPL (in a terminal: `lein repl`, or from Emacs: open a
clj/cljs file in the project, then do `M-x cider-jack-in`. Make sure
CIDER is up to date).

In the REPL do

```clojure
(run)
(browser-repl)
```

The call to `(run)` does two things, it starts the webserver at port
10555, and also the Figwheel server which takes care of live reloading
ClojureScript code and CSS. Give them some time to start.

Running `(browser-repl)` starts the Weasel REPL server, and drops you
into a ClojureScript REPL. Evaluating expressions here will only work
once you've loaded the page, so the browser can connect to Weasel.

When you see the line `Successfully compiled "resources/public/pumpkin.js"
in 15.509 seconds.`, you're ready to go. Browse to
`http://localhost:10555` and enjoy.

## Deploying to Heroku

This assumes you have a
[Heroku account](https://signup.heroku.com/dc), have installed the
[Heroku toolbelt](https://toolbelt.heroku.com/), and have done a
`heroku login` before.

``` sh
git init
git add -A
git commit
heroku create
git push heroku master:master
heroku open
```

## Running with Foreman

Heroku uses [Foreman](http://ddollar.github.io/foreman/) to run your
app, which uses the `Procfile` in your repository to figure out which
server command to run. Heroku also compiles and runs your code with a
Leiningen "production" profile, instead of "dev". To locally simulate
what Heroku does you can do:

``` sh
lein with-profile -dev,+production uberjar && foreman start
```

Now your app is running at
[http://localhost:5000](http://localhost:5000) in production mode.

## License

Copyright © 2014 Anna Pawlicka

Distributed under the Eclipse Public License.
