(defproject clj-vultr "0.1.0-SNAPSHOT"
  :description "Clojure library for accessing Vultr API."
  :url "https://github.com/sanel/clj-vultr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http-lite "0.3.0"]
                 [org.clojure/data.json "0.2.6"]]
  :global-vars {*warn-on-reflection* true})
