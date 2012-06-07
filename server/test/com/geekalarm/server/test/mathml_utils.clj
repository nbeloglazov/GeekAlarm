(ns com.geekalarm.server.test.mathml-utils
  (:use com.geekalarm.server.mathml-utils
        clojure.test))


(deftest test-cljml
  (is (= (cljml (/ 1 2))
         [:mfrac [:mn 1] [:mn 2]]))
  (is (= (cljml "hello")
         [:mi "hello"]))
  (is (= (cljml 42)
         [:mn 42]))
  (is (= (cljml [[1 2] [3 4]])
         [:mtable [:mtr [:mtd [:mn 1]]
                        [:mtd [:mn 2]]]
                  [:mtr [:mtd [:mn 3]]
                        [:mtd [:mn 4]]]]))
  (is (= (cljml :test)
         [:mi ":test"]))
  (is (= (cljml :mfrac [[:mn 1] [:mn 2]])
         [:mfrac [:mn 1] [:mn 2]])))



  

