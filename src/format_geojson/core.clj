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
