(ns kiries.layout
  (:use hiccup.core)
  (:use hiccup.page))

(defn full-layout [title & content]
  (html5
   [:head
    (include-css "/kibana/common/css/bootstrap.light.min.css")
    (include-css "/kibana/common/css/bootstrap-responsive.min.css")
    (include-css "/kibana/common/css/font-awesome.min.css")
    [:title title]]
   [:body
    [:div.container-fluid.main
     [:div.row-fluid
      content]]]))
