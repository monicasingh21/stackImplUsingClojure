(ns my-project.core
  (:require [monger.core :as mg])
  (:require [monger.collection :as mc])
  (:import org.bson.types.ObjectId)
  (:gen-class))

(def input (list))
(def top (atom -1))
(def MAX 5)
(def conn (mg/connect))
(def db (mg/get-db conn "mydb"))
(def coll "stack")


(defn push [val]
  (if (>= @top (- MAX 1))
    (println "Stack Overflow!!!")
    (do
      (swap! top inc)
      (def input (list* val input))
      (println "Pushing the element into stack, updated Stack is:" input)
      (mc/insert-and-return db coll {:_id (ObjectId.) :operation "PUSH" :updatedList input})
      )      
    ))


(defn popst
  []
  (if (< @top 0)
    (println "Stack Empty!!!")
    (do
      (swap! top dec)
      (def input (rest input))
      (println "Element deleted, updated Stack is:" input)
      (mc/insert-and-return db coll {:operation "POP" :updatedList input})
      )
      
    ))

(defn peekst []
  (if (< @top 0)
    (println "Stack Empty!!!")
    (println "Top element in Stack :" (nth input 0))))

(defn show_all_operations []
  (def stack_data (mc/find-maps db coll))
  (loop [x 0]
    (when (< x (count stack_data))
       (let [sdata (nth stack_data x)]
         (def operation ((select-keys sdata [:operation]) :operation))
         (def updatedList ((select-keys sdata [:updatedList]) :updatedList))
       )
      (println "Operation:  " operation "\t Updated List:  " updatedList)
      (recur (+ x 1))))
  )

(defn -main
  [& args]
  (if (mc/exists? db "stack")
    (mc/drop db "stack"))

  (def con 0)
  (println "Welcome to Stack World!!!")
  (while (not= con 5)
    (do
      (println "Please select your input : ")
      (println " 1. Push element into stack")
      (println " 2. Pop element from stack")
      (println " 3. Peek element from stack")
      (println " 4. Display elements of stack")
      (println " 5. Exit")
      (def con (Integer. (re-find #"\d+" (read-line))))
      (cond
        (= con 1)
        (do
          (println "Please enter your value to insert into stack : ")
          (let [value (Integer. (re-find #"\d+" (read-line)))] (push value)))
        (= con 2) (popst)
        (= con 3) (peekst)
        (= con 4)
        (do
          (mc/find-maps db "stack")
          (println "Elements in stack: " input))
        (= con 5) (println "Thanks!!!")
        :else (println "Invalid Option, please retry..."))))
        

        (println "Number of PUSH operations done" (mc/count db coll {:operation "PUSH"}))
        (println "Number of POP operations done" (mc/count db coll {:operation "POP"}))
        
        (println "--------------------------------------")
        (show_all_operations)
        (println "--------------------------------------")
        (println)
        )
