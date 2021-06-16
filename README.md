# JSON_Database

The purpose of this application is a modeling of a simple database with Client-Server architecture. A data in database saved in JSON files.
The server can receive requests from clients, and clients can get responses from the server.

The feature of this application is a cuncurrent connections to the server and keep data consistantly and correctly.
All requests get from clients parallel but put into a queue if persistent process is in action. As soon as transaction commited or finished by another cause, client receive response and another query to database is processed.
