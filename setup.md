# SimpleLedger Setup (Tomcat 9)

This guide walks through deploying `SimpleLedger` to Tomcat 9.

## 1) Create the database schema

Run the SQL file first:

```sql
-- from this repository
schema.sql
```

Example:

```bash
mysql -u <db_user> -p < "schema.sql"
```

This creates:
- `simple_ledger_sch`
- all required tables
- seed transaction types

## 2) Install Java 8 and Maven

- Install JDK 8 (project is built with Java 8 target).
- Install Maven 3.x.
- Verify:

```bash
java -version
mvn -version
```

## 3) Install Tomcat 9

- Download and extract/install Apache Tomcat 9.
- Define `CATALINA_HOME` (optional but helpful).

## 4) Configure JDBC DataSource in Tomcat `context.xml`

Edit Tomcat context file:
- Global: `${CATALINA_HOME}/conf/context.xml`
- Or app-specific context file under `${CATALINA_HOME}/conf/Catalina/localhost/`

Add this `Resource` (credentials masked):

```xml
<Resource name="jdbc/SimpleLedgerDB" auth="Container" type="javax.sql.DataSource"
          maxTotal="100" maxIdle="30" maxWaitMillis="10000"
          username="<db_username>" password="<db_password>"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/simple_ledger_sch"/>
```

## 5) Add MySQL JDBC driver to Tomcat

Place MySQL Connector/J JAR (8.x) in:

`$CATALINA_HOME/lib`

Then restart Tomcat after adding it.

## 6) Build the WAR

From project root:

```bash
mvn clean package
```

WAR output:

`target/SimpleLedger-1.0-SNAPSHOT.war`

## 7) Deploy to Tomcat

Copy WAR to:

`$CATALINA_HOME/webapps/`

Tomcat will auto-deploy it.

## 8) Start Tomcat and verify

Start Tomcat and check logs:
- `${CATALINA_HOME}/logs/catalina.out` (Linux/macOS)
- `${CATALINA_HOME}/logs/` on Windows

Base API path (from app config):

`/api`

Typical URL:

`http://localhost:8080/SimpleLedger-1.0-SNAPSHOT/api`

If you rename WAR to `SimpleLedger.war`, base app URL becomes:

`http://localhost:8080/SimpleLedger/api`

## 9) Quick smoke checks

- List accounts:
  - `GET /api/Accounts`
- List transactions:
  - `GET /api/Transactions`

If DataSource setup is wrong, responses will return SQL errors in JSON.

