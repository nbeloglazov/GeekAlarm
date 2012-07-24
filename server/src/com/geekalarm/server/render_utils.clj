(ns com.geekalarm.server.render-utils
  (require [com.geekalarm.server.mathml-utils :as mathml])
  (import [net.sourceforge.jeuclid.context Parameter]
	  [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn get-layout []
  (let [layout (net.sourceforge.jeuclid.context.LayoutContextImpl/getDefaultLayoutContext)]
    (.setParameter layout Parameter/MATHSIZE 30)
    (.setParameter layout Parameter/MATHBACKGROUND java.awt.Color/WHITE)
    layout))


(defn parse-node [str]
  (let [parser (net.sourceforge.jeuclid.parser.Parser/getInstance)
	source (javax.xml.transform.stream.StreamSource. (java.io.StringReader. str))]
    (.parse parser source)))

(defn save-image [node]
  (let [layout (get-layout)
        converter (net.sourceforge.jeuclid.converter.Converter/getInstance)]
    (.convert converter node (java.io.File. "/home/nikelandjelo/res.png") "image/png" layout)))

(defn image-to-stream [image]
  (let [output (ByteArrayOutputStream.)]
    (javax.imageio.ImageIO/write image "PNG" output)
    (ByteArrayInputStream. (.toByteArray output))))

(defn cljml-to-stream [cljml]
  (if (coll? cljml)
   (let [layout (get-layout)
         converter (net.sourceforge.jeuclid.converter.Converter/getInstance)]
     (-> (mathml/cljml-to-str cljml)
         (parse-node)
         (#(.render converter % layout))
         (image-to-stream)))
   cljml))

(defn cljml-to-image [cljml]
  (->> (mathml/cljml-to-str cljml)
       (parse-node)
       (save-image)))

(defn render-cljml-task [task]
  (-> task
      (update-in [:question] cljml-to-stream)
      (update-in [:choices] #(map cljml-to-stream %))))
