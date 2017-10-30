# Asset Management Application
Further development is needed to make the application production ready

* Replace Lombok with **JPA + Hibernate** and persist the accounts and transactions in a centralized **database**
* Add **spring-data** dependency to access the data from the repositories
* Replace the ReentrantLock with EntityManagers OPTIMISTIC locking 
* Use property files for the database configurations (application.properties, application-prod.properties, application-dev.properties...) 
* Use internationalized notifications by defining the messages in messages.properties


