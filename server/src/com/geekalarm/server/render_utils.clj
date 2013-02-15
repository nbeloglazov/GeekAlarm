(ns com.geekalarm.server.render-utils
  (require [com.geekalarm.server.mathml-utils :as mathml])
  (import [java.io ByteArrayOutputStream]
          [org.scilab.forge.jlatexmath TeXConstants TeXIcon TeXFormula]))

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
  (if (string? val)
    (image-to-stream (latex val))
    val))

(defn render-task [task]
  (-> task
      (update-in [:question] render-to-stream)
      (update-in [:choices] #(map render-to-stream %))))

