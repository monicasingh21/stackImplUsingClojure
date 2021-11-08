(ns my-project.userexception
  (:gen-class :extends java.lang.Exception))

(defn userexception [message]
  supers(message))
