(ns format-geojson.dev
  (:require [format-geojson.core :refer :all]))

(defn- get-location [symbol data]
  (let [location (get data symbol)
        lat (nth location 1)
        long (nth location 2)]
    [long lat]))

(def madison-data
  ; in the format: name, location, function that can extract the coordinates from a single data point
  [[:bus
    "https://data.cityofmadison.com/api/views/f5sy-kcer/rows.json?accessType=DOWNLOAD"
    (partial get-location (keyword "Stop Location"))]
   [:polling-places
    "https://data.cityofmadison.com/api/views/rtyh-6ucr/rows.json?accessType=DOWNLOAD"
    (partial get-location :Address)]
   [:public-libraries
    "https://data.cityofmadison.com/api/views/p4au-pwd2/rows.json?accessType=DOWNLOAD"
    (partial get-location :Location)]
   [:vacant-land-sales
    "https://data.cityofmadison.com/api/views/iig4-49xp/rows.json?accessType=DOWNLOAD"
    (partial get-location :Situs)]
   ;; [:city-events
   ;;  "https://data.cityofmadison.com/api/views/t5vc-2fm7/rows.json?accessType=DOWNLOAD"
   ;;  (partial get-location :Location)]
   ])

(comment
  (map #(let [name (subs (str (get % 0) ".geojson") 1)
              url (get % 1)
              extract-location-fn (get % 2)]
          (spit name (json->geojson url extract-location-fn))) madison-data)
  )
