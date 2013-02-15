(ns com.geekalarm.server.render-utils
  (require [com.geekalarm.server.mathml-utils :as mathml])
  (import [net.sourceforge.jeuclid.context Parameter]
	  [java.io ByteArrayOutputStream]
          [org.scilab.forge.jlatexmath TeXConstants TeXIcon TeXFormula]))

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
    (.toByteArray output)))

(defn latex 
  [latex-txt & {:keys [color background border text-size] :or {color java.awt.Color/black
                                                               background java.awt.Color/white
                                                               border [0 0 0 0]
                                                               text-size 30}}]
  (let [formula (org.scilab.forge.jlatexmath.TeXFormula. latex-txt)
        icon (doto (.createTeXIcon formula TeXConstants/STYLE_DISPLAY text-size)
               (.setInsets (apply #(java.awt.Insets. %1 %2 %3 %4) border)))
        image (java.awt.image.BufferedImage. (.getIconWidth icon) 
                                             (.getIconHeight icon) 
                                             java.awt.image.BufferedImage/TYPE_INT_ARGB)
        g2 (doto (.createGraphics image)
             (.setColor background)
             (.fillRect 0 0 (.getIconWidth icon) (.getIconHeight icon)))
        label (doto (javax.swing.JLabel.)
                (.setForeground color))]
    (do
      (.paintIcon icon label g2 0 0)
      image)))

(defn render-to-stream [val]
  (cond (coll? val)
        (let [layout (get-layout)
              converter (net.sourceforge.jeuclid.converter.Converter/getInstance)]
          (-> (mathml/cljml-to-str val)
              (parse-node)
              (#(.render converter % layout))
              (image-to-stream)))
        (string? val) (image-to-stream (latex val))
        :default val))

(defn cljml-to-image [cljml]
  (->> (mathml/cljml-to-str cljml)
       (parse-node)
       (save-image)))

(defn render-task [task]
  (-> task
      (update-in [:question] render-to-stream)
      (update-in [:choices] #(map render-to-stream %))))

