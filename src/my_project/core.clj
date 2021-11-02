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
      (def input (conj input val))
      (println "Pushing the element into stack, updated Stack is:" input)
      (mc/insert-and-return db coll {:_id (ObjectId.) :operation "PUSH" :updatedList input}))))


(defn show_all_operations []
  (def stack_data (mc/find-maps db coll))
  (loop [x 0]
    (when (< x (count stack_data))
      (let [sdata (nth stack_data x)]
        (def operation ((select-keys sdata [:operation]) :operation))
        (def updatedList ((select-keys sdata [:updatedList]) :updatedList)))
      (println "Operation:  " operation "\t Updated List:  " updatedList)
      (recur (+ x 1)))))

(defn -main
  [& args]
  (if (mc/exists? db "stack")
    (mc/drop db "stack"))

  (def con 0)
  (println "Welcome to Stack World!!!")
  (while (not= con 7)
    (do
      (println "Please select your input : ")
      (println " 1. Push element into stack")
      (println " 2. Pop element from stack")
      (println " 3. Peek element from stack")
      (println " 4. Display elements of stack")
      (println " 5. Delete an element from a particular index")
      (println " 6. Insert an element at a particular index")
      (println " 7. Exit")
      (def con (Integer. (re-find #"\d+" (read-line))))
      (cond
        (= con 1)
        (do
          (println "Please enter your value to insert into stack : ")
          (let [value (Integer. (re-find #"\d+" (read-line)))] (push value)))
        (= con 2) (try
                    (def input (pop input))
                    (println "Element deleted, updated Stack is: " input)
                    (mc/insert-and-return db coll {:operation "POP" :updatedList input})
                    (catch Exception e (println (str "caught exception: " (.toString e)))))
        (= con 3) (println "Top element in the stack: " (peek input))
        (= con 4)
        (do
          (mc/find-maps db "stack")
          (println "Elements in stack: " input))
        (= con 5) (if (> (count input) 0)
                    (do
                      (println "Please enter the position you want to delete: ")
                      (let [pos (Integer. (re-find #"\d+" (read-line)))]
                        (def input (apply list (remove #{(nth input pos)} input)))
                        (println "Element deleted, updated list: " input)
                        (mc/insert-and-return db coll {:_id (ObjectId.) :operation "DLTATPOST" :updatedList input})))
                    (println "Stack is empty !!!!"))
        (= con 6) (if (< (count input) MAX)
                    (do
                      (println "Please enter the position and value you want to insert: ")
                      (let [pos (Integer. (re-find #"\d+" (read-line)))
                            value (Integer. (re-find #"\d+" (read-line)))]
                        (def input (apply list (concat (concat (take pos input) (list value)) (take-last (- (count input) pos) input))))
                        (println "Element inserted, updated list: " input)
                        (mc/insert-and-return db coll {:_id (ObjectId.) :operation "INSRTATPOST" :updatedList input})))
                    (println "Stack overflow !!!!"))
        (= con 7) (println "Thanks!!!")
        :else (println "Invalid Option, please retry..."))))


  (println "Number of PUSH operations done" (mc/count db coll {:operation "PUSH"}))
  (println "Number of POP operations done" (mc/count db coll {:operation "POP"}))

  (println "--------------------------------------")
  (show_all_operations)
  (println "--------------------------------------")
  (println))