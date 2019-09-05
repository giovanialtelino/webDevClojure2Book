-- :name save-message! :! :1
-- :doc creates a new message
INSERT INTO guestbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

-- :name get-messages :? :*
-- :doc selects all avaliable messages
SELECT * FROM guestbook
