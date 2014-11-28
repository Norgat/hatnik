(ns hatnik.web.client.form.github-issue
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hatnik.web.client.z-actions :as action]
            [hatnik.web.client.utils :as u]
            [hatnik.schema :as schm])
  (:use [clojure.string :only [split replace]]
        [hatnik.web.client.form.github-repo-check :only [github-repo-on-change]]))

(defn github-issue-component [data owner]
  (reify
    om/IInitState
    (init-state [this]
      {:repo-status
       (if (empty? (:gh-repo data))
         "has-warning"
         "has-success")
       :timer nil})
    om/IRenderState
    (render-state [this state]
      (dom/div nil
               (u/form-field {:data data
                              :field :gh-repo
                              :id "gh-repo"
                              :title "Repository"
                              :validator #(:repo-status state)
                              :text-on-fail "Repository isn't exist."
                              :placeholder "user/repo or organization/repo"
                              :type :text
                              :on-change #(let [repo (.. % -target -value)]
                                            (github-repo-on-change repo (:timer state) owner)
                                            (om/update! data :gh-repo repo))})
               (u/form-field {:data data
                              :field :title
                              :id "gh-issue-title"
                              :title "Title"
                              :validator schm/TemplateTitle
                              :type :text
                              :popover "supported variables: {{library}} {{version}} {{previous-version}}"})
               (u/form-field {:data data
                              :field :body
                              :id "gh-issue-body"
                              :title "Body"
                              :validator schm/TemplateBody
                              :type :textarea
                              :popover "supported variables: {{library}} {{version}} {{previous-version}}"})))))
