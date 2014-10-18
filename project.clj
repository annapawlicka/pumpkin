(defproject pumpkin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
                 [leiningen "2.5.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.taoensso/sente "1.1.0"]

                 ;; Server
                 [ring "1.3.1"]
                 [compojure "1.2.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.json "0.2.5"]

                 ;; UI
                 [racehub/om-bootstrap "0.3.1"]
                 [om "0.8.0-alpha1"]
                 [sablono "0.2.22"]
                 [com.andrewmcveigh/cljs-time "0.2.1"]

                 ;; Dev environment
                 [enlive "1.1.5"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.0-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "pumpkin.jar"

  :cljsbuild {:builds
              {:pumpkin {:source-paths ["src/cljs"]
                         :compiler {:output-to     "resources/public/js/pumpkin.js"
                                    :output-dir    "resources/public/js/out"
                                    :source-map    "resources/public/js/out.js.map"
                                    :preamble      ["react/react.min.js"]
                                    :externs       ["react/externs/react.js" "resources/public/js/d3.js"
                                                    "resources/public/js/dimple.js"]
                                    :optimizations :none
                                    :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns pumpkin.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]
                   :figwheel {:http-server-root "public"
                              :port 3449
                              :css-dirs ["resources/public/css"]}
                   :env {:is-dev true}
                   :cljsbuild {:builds {:pumpkin {:source-paths ["env/dev/cljs"]}}}}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :aot :all
                       :cljsbuild {:builds {:pumpkin
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :whitespace
                                              :closure-warnings {:externs-validation :off
                                                                 :non-standard-jsdoc :off}}}}}}})
