This library is a wrapper around a JDBC driver so that all executed queries can be recorded to different
locations like a file,console or any other remote server via plugins. The plugins must be registered  at startup time.

Current features include:

1. Capture all queries and stored procedure sent to the db server through the JDBC driver
2. Configure different types of listeners(which listen to events) to record to different locations - Currently,
   console and file are supported
3. Filter the queries that are being logged. Ability to configure filters at startup and use predefined or custom filters.
   Currently, there are 3 types of filter : ExactMatchSql, AllowAllSql, DenyAllSql

This has been tested with jdk 1.6.20 only.

Any new features/bug fix request,please create a new issue on github.

Detailed documentation on github wiki: https://github.com/gokulesh/SqlRecorder/wiki/Configuration-and-Usage
