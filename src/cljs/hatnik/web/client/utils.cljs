(ns hatnik.web.client.utils
  (:require [schema.core :as s]
            [goog.dom :as gdom]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn validate
  "Validates data using given schema. Returns one of two
  css classes: has-success or has-error"
  [schema data]
  (cond
   (empty? data) "has-warning"
   (s/check schema data) "has-error"
   :else "has-success"))

(defn mobile?
  "Identifies whether current viewport is mobile viewport."
  []
  (-> (gdom/getViewportSize)
      (.-width)
      (< 768)))

(defn warning-helper [validator data text-on-fail]
  (if (= "" data)
    "Field can't be empty!"
    text-on-fail))

(defn form-field
  "Creates standard form field"
  [{:keys [data field id title
           type placeholder validator
           feedback on-change popover text-on-fail]}]
  
  (let [is-valid? (if (fn? validator)
                   (validator)
                   (validate validator
                             (field data)))]
    (dom/div #js {:className (str "form-group "
                                  (if feedback "has-feedback " "")
                                  is-valid?)}
             (dom/label #js {:htmlFor id
                             :className "control-label col-sm-2 no-padding-right"}
                        title)
             (dom/div #js {:className "col-sm-10"}
                      (let [attrs #js {:type "text"
                                       :className "form-control"
                                       :id id
                                       :value (field data)
                                       :placeholder placeholder
                                       :data-content popover
                                       :data-toggle "popover"
                                       :data-placement "auto"
                                       :data-trigger "focus"
                                       :onChange (or on-change
                                                     #(om/update! data field (.. % -target -value)))}]
                        (case type
                          :text (dom/input attrs)
                          :textarea (dom/textarea attrs)))
                      feedback)
             
             (when (not= "has-success" is-valid?)               
               (dom/div nil
                        (dom/div #js {:className "col-sm-2"})
                        (dom/div #js {:className "col-sm-10"}
                                 (dom/p #js {:className (case is-valid?
                                                          "has-warning" "text-warning"
                                                          "has-error" "text-error")}
                                        (warning-helper validator (field data) text-on-fail))))))))
