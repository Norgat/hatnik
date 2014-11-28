(ns hatnik.web.client.form.github-repo-check
  (:require [om.core :as om :include-macros true]
            [hatnik.web.client.z-actions :as action])
  (:use [clojure.string :only [split replace]]))

(defn set-repo-status [owner status]
  (om/set-state! owner :repo-status status))

(defn github-repos-handler [reply owner repo]
  (let [rest (js->clj reply)
        exists? (some #(= repo (get % "name")) rest)]
    (set-repo-status owner
                     (if exists? "has-success" "has-error"))))

(defn github-repo-on-change [gh-repo timer owner]
  (js/clearTimeout timer)
  (set-repo-status owner "has-warning")
  (let [[user repo] (split gh-repo "/")]
    (when-not (or (nil? repo) (nil? user)
                  (= "" user) (= "" repo))
      (let [timer (js/setTimeout
                   (fn []
                     (action/get-github-repos
                      user
                      #(github-repos-handler % owner repo)
                      #(set-repo-status owner "has-error")))
                   1000)]
        (om/set-state! owner :timer timer)))))
