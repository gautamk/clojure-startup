(ns clj-startup.core
    (:require [net.cgrand.enlive-html :as html]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(def  *places-url* "http://www.mtcbus.org/Places.asp")
(def  *routes-url* "http://www.mtcbus.org/Routes.asp")


(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-set [resource]
    (sorted-set
        (map 
            html/text(
                html/select (fetch-url resource) [:option]
                )
            )
        )
    )

(defn fetch-places []
    (fetch-set *places-url*)
)
(defn fetch-routes []
    (fetch-set *routes-url*)
    )


(defn -main[]
    (doseq [places (map #(apply str [\" %1 \"] ) (apply eval '(fetch-places) ) )] 
      (println  places))
  )