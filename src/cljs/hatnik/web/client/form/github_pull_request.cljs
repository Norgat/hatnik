(ns hatnik.web.client.form.github-pull-request
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hatnik.web.client.z-actions :as action]
            [hatnik.web.client.utils :as u]
            [hatnik.schema :as schm])
  (:use [clojure.string :only [split replace]]
        [hatnik.web.client.form.github-repo-check :only [github-repo-on-change]]))

(defn pull-request-operation [data owner]
  (reify
    om/IRender
    (render [this]
      (let [id (:id data)
            oper (:data data)
            div-id (str "gh-pull-req-op-" id)
            content-div-id (str "gh-pull-req-op-col-" id)]
        (dom/div #js {:className "clearfix operation"}
                 (u/form-field {:data oper
                                :field :file
                                :id (str "gh-pull-req-op-file-" id)
                                :title "File"
                                :validator (schm/string-of-length 1 1024)
                                :placeholder "e.g. project.clj"
                                :type :text
                                :popover "path to the file to be edited relative to repo root"})
                 (u/form-field {:data oper
                                :field :regex
                                :id (str "gh-pull-req-op-regex-" id)
                                :title "Regex"
                                :validator (schm/string-of-length 1 128)
                                :placeholder "e.g. {{library}} \"[^\"]+\""
                                :type :text
                                :popover (str "supports template variables like {{library}} and others, "
                                              "uses java regex syntax")})
                 (u/form-field {:data oper
                                :field :replacement
                                :id (str "gh-pull-req-op-repl-" id)
                                :title "Replacement"
                                :validator (schm/string-of-length 1 128)
                                :placeholder "e.g. {{library}} \"{{version}}\""
                                :type :text
                                :popover (str "supports template variables like {{library}} and others, "
                                              "supports backreferences, uses java regex syntax")})
                 (dom/div #js {:className "dropdown pull-right"}
                          (dom/div #js {:className "btn btn-danger"
                                        :onClick #((:delete data) id)}
                                   "Delete")))))))

(defn add-new-operation [data]
  (om/transact! data #(conj % {:file "" :regex "" :replacement ""})))

(defn pull-request-operations-list [data owner]
  (reify
    om/IRender
    (render [this]
      (letfn [(remove-nth [coll ind]
                (keep-indexed (fn [i v]
                                (if (not= i ind) v nil))
                              coll))
              (delete-operation [id]
                (om/transact! data
                  #(vec (remove-nth % id))))]
        (dom/div #js {:className "clearfix"}
                 (dom/h4 nil "Operations")
                 (apply dom/div #js {:className "operations"}
                        (map-indexed (fn [id operation]
                                       (om/build pull-request-operation
                                                 {:id id
                                                  :data operation
                                                  :delete delete-operation}))
                                     data))
                 (dom/div #js {:className "btn btn-primary pull-right"
                               :onClick #(add-new-operation data)}
                          "Add"))))))

(defn github-pull-request-component [data owner]
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
                              :id "gh-title"
                              :title "Title & commit"
                              :validator schm/TemplateTitle
                              :type :text
                              :popover "supported variables: {{library}} {{version}} {{previous-version}}"})
               (u/form-field {:data data
                              :field :body
                              :id "gh-body"
                              :title "Body"
                              :validator schm/TemplateBody
                              :type :textarea
                              :popover (str "supported variables: {{library}} {{version}} {{previous-version}} "
                                            "{{results-table}}")})
               (om/build pull-request-operations-list (:file-operations data))))))
