(ns hatnik.web.client.form-components
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hatnik.web.client.z-actions :as action]
            [hatnik.web.client.app-state :as state]))

(defn project-adding-from [data]
  (dom/div 
   #js {:className "modal-content"}
   (dom/div #js {:className "modal-header"}
            (dom/h4 #js {:className "modal-title"}
                    "Add project"))))


;; For selecting action form header

(defn email-action-form-header [data]
  (reify
    om/IRender
    (render [this]
      (dom/h4 #js {:className "modal-title"}
              "Add email notification"))))

(defn email-update-header [data]
  (reify
    om/IRender
    (render [this]
      (dom/div 
       nil
       (dom/h4 #js {:className "modal-title"}
               "Edit email notification"
       (dom/button #js {:className "btn btn-danger pull-right"
                        :onClick #(action/delete-action 
                                   (get 
                                    (deref (-> @data :ui :current-action)) "id"))} 
                   "Delete"))))))

(def action-forms-headers 
  {:email-action email-action-form-header
   :email-edit-action email-update-header})

(defn action-form-header [data owner]
  (apply
   (get action-forms-headers (-> data :ui :form-type))
   [data]))

;; For selecting action form body

(defn input-handle [e]
  (state/set-current-artifact-value (.. e -target -value)))

(defn email-template-handle [e]
  (state/set-current-email-template (.. e -target -value)))

(defn email-action-form-body [data]
  (dom/div #js {:className "form-group"}
           (dom/label #js {:for "emain-input"} "Email")
           (dom/input #js {:type "email"
                           :className "form-control"
                           :id "emain-input"
                           :value (-> data :data :user :email)
                           :disabled "disabled"})
           (dom/div #js {:className "form-group"}
                    (dom/label #js {:for "emain-body-input"} "Email body")
                    (dom/textarea #js {:cols "40"
                                       :className "form-control"
                                       :id "emain-body-input"
                                       :value (-> data :ui :email-template)
                                       :onChange email-template-handle}))))

(defn artifact-input [data]
  (dom/div #js {:className "form-group has-warning"
                :id "artifact-input-group"}
           (dom/label #js {:for "artifact-input"} "Library")
           (dom/input #js {:type "text"
                           :className "form-control"
                           :id "artifact-input"
                           :placeholder "e.g. org.clojure/clojure"
                           :onChange input-handle
                           :value (-> data :ui :email-artifact-value)})))

(defn action-type-change-handler [owner e]
  (om/set-state! owner :action-type
                 (keyword (.. e -target -value))))

(defn action-type-selector [data owner]
  (dom/div #js {:className "form-group"}
           (dom/label nil "Action type")
           (dom/select #js {:ref "action-type"
                            :className "form-control"
                            :onChange #(action-type-change-handler owner %)}
                       (dom/option #js {:value "noop"} "Noop")
                       (dom/option #js {:value "email"} "Email"))))

(defn add-action-form-body [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:action-type :noop})
    om/IRenderState
    (render-state [this state]
      (dom/form
       #js {:id "email-action-form"}
       (artifact-input data)
       (action-type-selector data owner)
       (let [action-type (:action-type state)]
         (cond 
          (= :email action-type) (email-action-form-body data)
          (= :noop action-type) (dom/div nil "")
          :else (dom/div nil "")))))))


(def action-form-bodys 
  {:email-action #(om/build add-action-form-body %)
   :email-edit-action #(email-action-form-body %)})

(defn action-form-body [data body]
  (reify
    om/IRender
    (render [this]
      (om/build add-action-form-body data))))

;; For selecting footer

(defn email-action-footer [data]
  (dom/div nil
           (dom/button #js {:className "btn btn-primary pull-left"
                            :onClick #(action/send-new-email-action (-> @data :ui :current-project))} "Submit")
           (dom/button #js {:className "btn btn-default"
                            :onClick #(action/test-new-email-action (-> @data :ui :current-project))} "Test")))

(defn email-edit-footer [data]
  (dom/div 
   nil
   (dom/button #js {:className "btn btn-primary pull-left"
                    :onClick #(action/update-email-action 
                               (-> @data :ui :current-project)
                               (get 
                                (deref (-> @data :ui :current-action)) "id"))} "Update")
   (dom/button #js {:className "btn btn-default"
                    :onClick #(action/test-new-email-action (-> @data :ui :current-project))} "Test")))

(def action-form-footers
  {:email-action email-action-footer
   :email-edit-action email-edit-footer})

(defn action-form-footer [data body]
  (reify
    om/IRender
    (render [this]
      (apply 
       (get action-form-footers (-> data :ui :form-type))
       [data]))))


(defn email-action-form [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div 
       #js {:className "modal-dialog"}
       (dom/div #js {:className "modal-content"}
                (dom/div #js {:className "modal-header"} (om/build action-form-header data))
                (dom/div #js {:className "modal-body"}  (om/build action-form-body data))
                (dom/div #js {:className "modal-footer"} (om/build action-form-footer data))
                )))))
