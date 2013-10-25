(ns format-geojson.core
  (:require [cheshire.core :as json]))

(defn- get-json-from-file [filename]
  (json/parse-string (slurp filename)))

(defn- format-geojson-feature
  "Makes a GeoJSON 'Point' map from a vector from the given json"
  [extract-location-fn data]
  (let [coordinates (extract-location-fn data)]
    (println data
             coordinates
             extract-location-fn)
    {:type "Feature"
     :geometry
     {:type "Point"
      :coordinates coordinates}
     :properties data}))

(defn- make-geojson-features [extract-location-fn data-list]
  (map (partial format-geojson-feature extract-location-fn) data-list))

(defn- format-data-with-keys [keys data-list]
  (map #(apply hash-map (interleave keys %))
       data-list))

(defn- get-column-keys
  "Gets the keys for a GeoJSON 'Point' vector based on the data"
  [data]
  (vec (map keyword (map #(get % "name")
                         (get-in data ["meta" "view" "columns"])))))

(defn json->geojson [filename extract-location-fn]
  (let [json (get-json-from-file filename)
        keys (get-column-keys json)
        data-list (get json "data")
        data-with-keys (format-data-with-keys keys data-list)
        geojson-points (make-geojson-features extract-location-fn data-with-keys)]
    (json/generate-string {:type "FeatureCollection"
                           :features geojson-points})))

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
