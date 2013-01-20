(ns clj-startup.core
  (:require [net.cgrand.enlive-html :as html]))
(use 'korma.db)
(use 'korma.core)


(defdb mys (mysql 
    {:user "mtc" :password "mtc" :db "mtc" :host "localhost" :port "3306" }))

(defentity places_table
  (table :places)
  (database mys))

(defentity routes_table
  (table :routes)
  (database mys))

(defentity route_place_subscription
  (table :route_place_subscription)
  (database mys))


(defn insert-places [places]
    (insert places_table
      (values places)))

(defn insert-routes [routes]
  (insert routes_table
    (values routes)))

(defn insert-route-place-subscription 
  [route place]
  (insert route_place_subscription
    (values {:route route :place place})))
(defn insert-places-for-route
  [route places]
  (doseq [place places]
    (insert-route-place-subscription route place)))

(def  *places-url* "http://www.mtcbus.org/Places.asp")
(def  *routes-url* "http://www.mtcbus.org/Routes.asp")
(defn stages-for-routes [route] (str *routes-url* "?cboRouteCode=" route))


(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-options-set [resource]
  (map 
      html/text(
          html/select (fetch-url resource) [:option])))

(defn fetch-stages-for [route]
  (map html/text (html/select 
                    (fetch-url route) 
                    [[:td (html/attr= :align "left") (html/attr= :colspan "6")] ])))


(defn fetch-places []
    (fetch-options-set *places-url*))

(defn fetch-routes []
    (fetch-options-set *routes-url*))

(defn truncate-and-populate-places []
   (exec-raw ["TRUNCATE TABLE places;"] )
  (insert-places
    (map (fn [pname] {:place_name pname}) (into (sorted-set)(fetch-places)))))

(defn truncate-and-populate-routes[]
  (exec-raw ["TRUNCATE TABLE routes;"])
  (insert-routes
    (map (fn [routename] {:route routename}) (into (sorted-set) (fetch-routes)))))

(defn truncate-route-place-subscription[]
  (exec-raw ["TRUNCATE TABLE route_place_subscription;"]))

(defn truncate-and-populate-route-place-subscriptions[]
  (truncate-route-place-subscription)
  (doseq [route (fetch-routes)]
    (insert-places-for-route route (fetch-stages-for (stages-for-routes route)))))

(defn -main[]
  (truncate-and-populate-places)
  (truncate-and-populate-routes)
  (truncate-and-populate-route-place-subscriptions))

  