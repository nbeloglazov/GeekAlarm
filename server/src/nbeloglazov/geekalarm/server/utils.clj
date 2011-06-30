(ns nbeloglazov.geekalarm.server.utils
  (import [net.sourceforge.jeuclid.context Parameter]))

(def layout (net.sourceforge.jeuclid.context.LayoutContextImpl/getDefaultLayoutContext))

(.setParameter layout Parameter/MATHSIZE 30)
(.setParameter layout Parameter/MATHBACKGROUND java.awt.Color/WHITE)


(defn convert-to-node [str]
  (let [parser (net.sourceforge.jeuclid.parser.Parser/getInstance)
	source (javax.xml.transform.stream.StreamSource. (java.io.StringReader. str))]
    (.parse parser source)))

(defn save-image [node]
  (let [converter (net.sourceforge.jeuclid.converter.Converter/getInstance)]
    (.convert converter node (java.io.File. "/home/nikelandjelo/res.png") "image/png" layout)))