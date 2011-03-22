STATUS:VOLATILE - NOT TO BE USED

NOTE: This is a highly volatile codebase as I am trying to get a prototype up and running. Once that is done, will do some changes 
and add more tests around it.


This library is meant to be wrapper around a jdbc library so that all executed queries can be recorded to a file,console or any other remote server
via plugins. The plugins can be registered  at startup time. 

Another purpose is to get the list of queries during a unit/integration test so that we can actually verify the count/actual queries.

This project was born because there is no active library which logs queries that are executed. Developers don't get an idea 
of the queries involved in a particular feature(more so if a ORM is used) until it is dropped in production and we are left 
with identifying what feature is executing this query. 

