(ns pumpkin.server
  (:require [clojure.core.async :as async :refer [<! <!! chan go-loop thread]]
            [clojure.java.io :as io]
            [pumpkin.dev :refer [is-dev? inject-devmode-html start-figwheel]]
            [compojure.core :refer [GET defroutes routes]]
            [compojure.route :refer [resources] :as r]
            [compojure.handler :refer [api] :as h]
            [taoensso.sente :as s]
            [ring.middleware.reload :as reload]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server] :as kit]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [net.cgrand.enlive-html :refer [deftemplate]]))

(let [{:keys [ch-recv send-fn ajax-post-fn
              ajax-get-or-ws-handshake-fn] :as sente-info}
      (s/make-channel-socket! {})]
  (def ring-ajax-get-ws ajax-get-or-ws-handshake-fn)
  (def ch-chsk          ch-recv)
  (def chsk-send!       send-fn))

(defn unique-id
  "Return a really unique ID (for an unsecured session ID).
  No, a random number is not unique enough. Use a UUID for real!"
  []
  (rand-int 10000))

(defn session-uid
  "Convenient to extract the UID that Sente needs from the request."
  [req]
  (get-in req [:session :uid]))

(deftemplate page
  (io/resource "index.html") [] [:body] (if is-dev? inject-devmode-html identity))

(defn index
  "Handle index page request. Injects session uid if needed."
  [req]
  {:status 200
   :session (if (session-uid req)
              (:session req)
              (assoc (:session req) :uid (unique-id)))
   :body (page)})

(defroutes my-routes
  (-> (routes
       (GET  "/"   req (#'index req))
       (GET  "/ws" req (#'ring-ajax-get-ws req))
       (resources "/")
       (resources "/react" {:root "react"})
       (r/not-found "<p>Page not found. I has a sad!</p>"))
      h/site))

(def route-handler
  (if is-dev?
    (reload/wrap-reload #'my-routes)
    #'my-routes))

(defmulti handle-event :id)

(defn session-status
  "Tell the server what state this user's session is in."
  [req]
  (when-let [uid (session-uid req)]
    (chsk-send! uid [:session/state :open])))

(defmethod handle-event :session/status
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (session-status ring-req))

(defmethod handle-event :dashboard/github-issues
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)]
      (when-let [uid (:uid session)]
        (let [{:keys [url refresh-rate]} ?data]
          (while true
            (http/get url {}
                 (fn [{:keys [status headers body error]}]
                   (if error
                     (println error)
                     (let [parsed-body (json/read-str body :key-fn keyword)]
                       (if-not (seq (:message parsed-body))
                         (do
                           (println "Pushing github issues")
                           (chsk-send! uid [:dashboard/github-issues parsed-body]))
                         (println "Message" (:message parsed-body)))))))
            (Thread/sleep refresh-rate))))))

(defmethod handle-event :dashboard/github-pulls
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)]
      (when-let [uid (:uid session)]
        (let [{:keys [url refresh-rate]} ?data]
          (while true
            (http/get url {}
                 (fn [{:keys [status headers body error]}]
                   (if error
                     (println error)
                     (let [parsed-body (json/read-str body :key-fn keyword)]
                       (if-not (seq (:message parsed-body))
                         (do
                           (println "Pushing github pull requests")
                           (chsk-send! uid [:dashboard/github-pulls parsed-body]))
                         (println "Message" (:message parsed-body)))))))
            (Thread/sleep refresh-rate))))))

(defmethod handle-event :dashboard/github-code-frequency
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)]
      (when-let [uid (:uid session)]
        (let [{:keys [url refresh-rate]} ?data]
          (while true
            (http/get url {}
                      (fn [{:keys [status headers body error]}]
                        (if error
                          (println error)
                          (let [parsed-body (json/read-str body :key-fn keyword)]
                            (if-not (seq (:message parsed-body))
                              (do
                                (println "Pushing github code frequency stats")
                                (chsk-send! uid [:dashboard/github-code-frequency
                                                 (->> parsed-body
                                                      (mapv (fn [[week additions deletions]]
                                                              [{:week week :type :additions :value additions}
                                                               {:week week :type :deletions :value deletions}]))
                                                      (apply concat)
                                                      (into []))]))
                              (println "Message" (:message parsed-body)))))))
            (Thread/sleep refresh-rate))))))

(defmethod handle-event :dashboard/github-contributors
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)]
      (when-let [uid (:uid session)]
        (let [{:keys [url refresh-rate]} ?data]
          (while true
            (http/get url {}
                 (fn [{:keys [status headers body error]}]
                   (if error
                     (println error)
                     (let [parsed-body (json/read-str body :key-fn keyword)]
                       (if-not (seq (:message parsed-body))
                         (do
                           (println "Pushing github contributors stats")
                           (chsk-send! uid [:dashboard/github-contributors parsed-body]))
                         (println "Message" (:message parsed-body)))))))
            (Thread/sleep refresh-rate))))))

(defmethod handle-event :default
  [{:as ev-msg :keys [?data]}]
  nil)

(defn event-loop
  "Handle inbound events."
  []
  (go-loop []
    (let [{:as ev-msg :keys [?data id]} (<! ch-chsk)]
      (println "- " id)
      (thread (handle-event ev-msg)))
    (recur)))

(defn run [& [port]]
  (event-loop)
  (defonce ^:private server
    (do
      (if is-dev? (start-figwheel))
      (let [port (Integer. (or port (env :port) 10555))]
        (print "Starting web server on port" port ".\n")
        (run-server route-handler {:port port
                                  :join? false}))))
  server)

(defn -main [& [port]]
  (run port))
