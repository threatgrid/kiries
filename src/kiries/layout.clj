(ns kiries.layout
  (:require [hiccup.page :as hp]))

(defn full-layout [title & content]
  (hp/html5
   [:head
    (hp/include-css "/kibana/css/bootstrap.light.min.css")
    (hp/include-css "/kibana/css/bootstrap-responsive.min.css")
    (hp/include-css "/kibana/css/font-awesome.min.css")
    (hp/include-css "/css/kiries.css")
    [:title title]]
   [:body
    [:div.container-fluid.main
     [:div.row-fluid
      content]]]))
