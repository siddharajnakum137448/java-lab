-- Run this as SYS (or SYSTEM) inside the PDB after Oracle is installed:
--   sql sys@localhost:1521/FREEPDB1 as sysdba  (Oracle 26ai Free)
--   sql system@localhost:1521/XEPDB1          (Oracle 21c XE)
-- Then run: @C:\Users\LENOVO\Desktop\Java-Project\scripts\setup_oracle_user.sql

CREATE USER skyport IDENTIFIED BY skyport123;
GRANT CREATE SESSION TO skyport;
GRANT CREATE TABLE TO skyport;
GRANT UNLIMITED TABLESPACE TO skyport;
EXIT;
