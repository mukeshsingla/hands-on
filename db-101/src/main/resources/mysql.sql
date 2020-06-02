-- mysql

-- establishing mysql session as root(Muk35h4u)
-- mysql -u root -p -h 127.0.0.1 -P 3306

-- Creating local user

SELECT user,authentication_string,plugin,host FROM mysql.user;

CREATE USER 'msingla'@'172.17.0.1' IDENTIFIED BY 'msingla';
GRANT ALL PRIVILEGES ON *.* TO 'msingla'@'172.17.0.1' WITH GRANT OPTION;

-- Paginating resultset using less (nopager - for removing pagination)
pager less -SFX


-- establishing mysql session as user
-- mysql -u msingla -p -h 127.0.0.1 -P 3306

-- Datasets
-- Learning SQL - Apache Ignite
create schema duckairlines;
source <path>/duckairdb.sql
GRANT ALL ON duckairlines.* TO 'msingla'@'localhost';

-- Sakila DB - Learning SQL
source <path>/sakila-db/sakila-schema.sql
source <path>/sakila-db/sakila-data.sql

GRANT ALL ON sakila.* TO 'msingla'@'172.17.0.1';


-- Datewise
------------

-- 2020-06-02
use mysql;
SET global general_log = 1;		-- Start logging queries
SET global log_output = 'table';	-- start logging queries in table
SELECT event_time, user_host, command_type, CONVERT(argument USING utf8) FROM general_log;	-- History of statements
SELECT CONVERT(argument USING utf8) FROM mysql.general_log;	-- History of statements

-- Subqueries
use sakila;

-- - Noncorrelated subqueries: independent of containing queries. Gets executed prior to containing statement
--   - Scalar subqueries - returns single row, single column and can appear on either side of condition using usual operators (=, <>, <, >, <=, >=)
SELECT customer_id, first_name, last_name FROM customer WHERE customer_id = (SELECT MAX(customer_id) FROM customer);

--   - Multiple-Row, Single-Column Subqueries (use IN / NOT IN)
--     - ALL operator:  allows you to make comparisons between a single value and every value in a set using usual operators (=, <>, <, >, <=, >=)
--       - <> ALL is same as NOT IN
SELECT customer_id, count(*) FROM rental GROUP BY customer_id HAVING count(*) > ALL 
	(SELECT count(*) FROM rental r INNER JOIN customer c ON r.customer_id = c.customer_id INNER JOIN address a ON c.address_id = a.address_id INNER JOIN city ct ON a.city_id = ct.city_id INNER JOIN country co ON ct.country_id = co.country_id WHERE co.country IN ('United States','Mexico','Canada') GROUP BY r.customer_id);
	
--     - ANY operator:  allows you to make comparisons between a single value and every value in a set using usual operators (=, <>, <, >, <=, >=)
--       - = ANY is same as IN
SELECT customer_id, SUM(amount) FROM payment GROUP BY customer_id HAVING SUM(amount) > ANY 
	(SELECT SUM(amount) FROM payment r INNER JOIN customer c ON r.customer_id = c.customer_id INNER JOIN address a ON c.address_id = a.address_id INNER JOIN city ct ON a.city_id = ct.city_id INNER JOIN country co ON ct.country_id = co.country_id WHERE co.country IN ('Bolivia','Paraguay','Chile') GROUP BY co.country);

SELECT fa.actor_id, fa.film_id FROM film_actor fa WHERE fa.actor_id IN (SELECT actor_id FROM actor WHERE last_name = 'MONROE') AND fa.film_id IN (SELECT film_id FROM film WHERE rating = 'PG');

--   - Multiple-Row, Multiple-Column Subqueries (use IN / NOT IN)
SELECT actor_id, film_id FROM film_actor WHERE (actor_id, film_id) IN (SELECT a.actor_id, f.film_id FROM actor a CROSS JOIN film f WHERE a.last_name = 'MONROE' AND f.rating = 'PG');

-- - Correlated subqueries: dependent on containing statements from which it references one or more columns. Gets executed for each candidate row
SELECT c.first_name, c.last_name FROM customer c WHERE 20 = (SELECT count(*) FROM rental r WHERE r.customer_id = c.customer_id);
SELECT c.first_name, c.last_name FROM customer c WHERE (SELECT SUM(p.amount) FROM payment p WHERE p.customer_id = c.customer_id) BETWEEN 180 AND 240;	-- with range conditions

--   - EXISTS: identifies relationships exists without regard for the quantity
SELECT c.first_name, c.last_name FROM customer c WHERE EXISTS (SELECT 1 FROM rental r WHERE r.customer_id = c.customer_id AND DATE(r.rental_date) < '2005-05-25');	-- conventionly 1 or * is used in subquery
SELECT c.first_name, c.last_name FROM customer c WHERE EXISTS 
	(SELECT r.rental_date, r.customer_id, 'ABCD' str, 2 * 3 / 7 nmbr FROM rental r WHERE r.customer_id = c.customer_id AND DATE(r.rental_date) < '2005-05-25');	-- doesn't matter what data subquery returns, only checks whether one or more results are returned

--   - NOT EXISTS
SELECT  a.first_name, a.last_name FROM actor a WHERE NOT EXISTS (SELECT 1 FROM film_actor fa INNER JOIN film f ON f.film_id = fa.film_id WHERE fa.actor_id = a.actor_id AND f.rating = 'R');

--   - Data Manipulation using correlated subqueries
-- UPDATE customer c SET c.last_update = (SELECT max(r.rental_date) FROM rental r WHERE r.customer_id = c.customer_id);		-- if no rental for customer, will set last_update = null
--UPDATE customer c SET c.last_update = (SELECT max(r.rental_date) FROM rental r WHERE r.customer_id = c.customer_id) WHERE EXISTS (SELECT 1 FROM rental r WHERE r.customer_id = c.customer_id); -- only update, if customer has rental

-- DELETE FROM customer WHERE 365 < ALL (SELECT datediff(now(), r.rental_date) days_since_last_rental FROM rental r WHERE r.customer_id = customer.customer_id);
-- DELETE FROM customer c WHERE 365 < ALL (SELECT datediff(now(), r.rental_date) days_since_last_rental FROM rental r WHERE r.customer_id = c.customer_id);

-- - Ways queries can be used
--   - as data sources
SELECT c.first_name, c.last_name, pymnt.num_rentals, pymnt.tot_payments FROM customer c INNER JOIN (SELECT customer_id,  count(*) num_rentals, sum(amount) tot_payments FROM payment GROUP BY customer_id ) pymnt ON c.customer_id = pymnt.customer_id;

--   - as data fabrication
SELECT pymnt_grps.name, count(*) num_customers 
 FROM (SELECT customer_id, count(*) num_rentals, sum(amount) tot_payments FROM payment GROUP BY customer_id ) pymnt 
  INNER JOIN (SELECT 'Small Fry' name, 0 low_limit, 74.99 high_limit UNION ALL SELECT 'Average Joes' name, 75 low_limit, 149.99 high_limit UNION ALL SELECT 'Heavy Hitters' name, 150 low_limit, 9999999.99 high_limit ) pymnt_grps ON pymnt.tot_payments BETWEEN pymnt_grps.low_limit AND pymnt_grps.high_limit 
 GROUP BY pymnt_grps.name;

--   - as task oriented subqueries
SELECT c.first_name, c.last_name, ct.city, sum(p.amount) tot_payments, count(*) tot_rentals 
 FROM payment p 
  INNER JOIN customer c ON p.customer_id = c.customer_id 
  INNER JOIN address a ON c.address_id = a.address_id 
  INNER JOIN city ct ON a.city_id = ct.city_id 
 GROUP BY c.first_name, c.last_name, ct.city;
--     - instead
SELECT c.first_name, c.last_name, ct.city, pymnt.tot_payments, pymnt.tot_rentals 
 FROM (SELECT customer_id, count(*) tot_rentals, sum(amount) tot_payments FROM payment GROUP BY customer_id ) pymnt 
  INNER JOIN customer c ON pymnt.customer_id = c.customer_id 
  INNER JOIN address a ON c.address_id = a.address_id 
  INNER JOIN city ct ON a.city_id = ct.city_id;

--   - as common table expression (CTE)
WITH actors_s AS (SELECT actor_id, first_name, last_name FROM actor WHERE last_name LIKE 's%'), 
 actors_s_pg AS (SELECT s.actor_id, s.first_name,  s.last_name, f.film_id, f.title FROM actors_s s INNER JOIN film_actor fa ON s.actor_id = fa.actor_id INNER JOIN film f ON f.film_id = fa.film_id WHERE f.rating = 'PG'), 
 actors_s_pg_revenue AS (SELECT spg.first_name, spg.last_name, p.amount FROM actors_s_pg spg INNER JOIN inventory i ON i.film_id = spg.film_id INNER JOIN rental r ON i.inventory_id = r.inventory_id INNER JOIN payment p ON r.rental_id = p.rental_id) 
 SELECT spg_rev.first_name, spg_rev.last_name, sum(spg_rev.amount) tot_revenue FROM actors_s_pg_revenue spg_rev GROUP BY spg_rev.first_name, spg_rev.last_name ORDER BY 3 DESC;
 
--   - as expression generators
SELECT 
  (SELECT c.first_name FROM customer c WHERE c.customer_id = p.customer_id) first_name,
  (SELECT c.last_name FROM customer c WHERE c.customer_id = p.customer_id) last_name,
  (SELECT ct.city FROM customer c INNER JOIN address a ON c.address_id = a.address_id INNER JOIN city ct ON a.city_id = ct.city_id WHERE c.customer_id = p.customer_id ) city, 
  sum(p.amount) tot_payments, 
  count(*) tot_rentals 
 FROM payment p GROUP BY p.customer_id;


SELECT a.actor_id, a.first_name, a.last_name FROM actor a 
 ORDER BY (SELECT COUNT(*) FROM film_actor fa WHERE fa.actor_id = a.actor_id) DESC;

INSERT INTO film_actor (actor_id, film_id, last_update)
 VALUES (
  (SELECT actor_id FROM actor
   WHERE first_name = 'JENNIFER' AND last_name = 'DAVIS'),
  (SELECT film_id FROM film
   WHERE title = 'ACE GOLDFINGER'),
  now()
 );

SELECT * FROM film f WHERE f.film_id IN (SELECT fc.film_id FROM film_category fc INNER JOIN category c ON fc.category_id = c.category_id WHERE c.name = 'Action');	-- non-correlated version
SELECT * FROM film f WHERE EXISTS (SELECT 1 FROM film_category fc INNER JOIN category c ON fc.category_id = c.category_id WHERE c.name = 'Action' AND f.film_id = fc.film_id); -- correlated version of above
SELECT a_roles.actor_id, a_roles.total_roles, actor_level.level 
 FROM (SELECT actor_id, COUNT(*) total_roles FROM film_actor GROUP BY actor_id) a_roles 
  INNER JOIN (SELECT 'Hollywood Star' level, 30 min_roles, 99999 max_roles UNION ALL SELECT 'Prolific Actor' level, 20 min_roles, 29 max_roles UNION ALL SELECT 'Newcomer' level, 1 min_roles, 19 max_roles) actor_level 
   ON a_roles.total_roles BETWEEN actor_level.min_roles AND actor_level.max_roles;	-- data fabrication


-- - Joins
SELECT f.film_id, f.title, count(i.inventory_id) num_copies FROM film f
  LEFT OUTER JOIN inventory i ON f.film_id = i.film_id
 GROUP BY f.film_id, f.title;	-- left outer join: includes all rows from left table and include columns from right table if join successful
 
SELECT f.film_id, f.title, count(i.inventory_id) num_copies FROM film f
  LEFT JOIN inventory i ON f.film_id = i.film_id
 GROUP BY f.film_id, f.title;	-- OUTER keyword is optional
 
SELECT f.film_id, f.title, i.inventory_id, r.rental_date FROM film f
  LEFT OUTER JOIN inventory i ON f.film_id = i.film_id
  LEFT OUTER JOIN rental r ON i.inventory_id = r.inventory_id
 WHERE f.film_id BETWEEN 13 AND 15;		-- three way outer joins

-- right outer join: opposite of left outer join

--   - cross joins
SELECT DATE_ADD('2020-01-01', INTERVAL (ones.num + tens.num + hundreds.num) DAY) dt
 FROM (
  SELECT 0 num UNION ALL
  SELECT 1 num UNION ALL
  SELECT 2 num UNION ALL
  SELECT 3 num UNION ALL
  SELECT 4 num UNION ALL
  SELECT 5 num UNION ALL
  SELECT 6 num UNION ALL
  SELECT 7 num UNION ALL
  SELECT 8 num UNION ALL
  SELECT 9 num) ones
   CROSS JOIN (
    SELECT 0 num UNION ALL
	SELECT 10 num UNION ALL
	SELECT 20 num UNION ALL
	SELECT 30 num UNION ALL
	SELECT 40 num UNION ALL
	SELECT 50 num UNION ALL
	SELECT 60 num UNION ALL
	SELECT 70 num UNION ALL
	SELECT 80 num UNION ALL
	SELECT 90 num) tens
   CROSS JOIN (
    SELECT 0 num UNION ALL
	SELECT 100 num UNION ALL
	SELECT 200 num UNION ALL
	SELECT 300 num) hundreds
 WHERE DATE_ADD('2020-01-01', INTERVAL (ones.num + tens.num + hundreds.num) DAY) < '2021-01-01'ORDER BY 1;	-- data fabrication using cartesian product
 
--   - natural joins
SELECT c.first_name, c.last_name, date(r.rental_date) FROM customer c NATURAL JOIN rental r;	-- Joins on the basis of same column name

-- - Conditional Logic
 SELECT first_name, last_name, CASE WHEN active = 1 THEN 'ACTIVE' ELSE 'INACTIVE' END activity_type FROM customer;	-- CASE expression
 
 SELECT c.first_name, c.last_name, 
   CASE
    WHEN active = 0 THEN 0
     ELSE
      (SELECT count(*) FROM rental r
       WHERE r.customer_id = c.customer_id)
   END num_rentals
 FROM customer c;	-- Searched case expression

-- 2020-06-01

show schemas;
use sakila;
show tables;
show warnings;

-- - Data Generation, Manipulation and Conversion
SELECT name, name LIKE '%Y' ends_in_y FROM category;
SELECT concat(first_name, ' ', last_name, ' has been a customer since ', date(create_date)) cust_narrative FROM customer;
SELECT INSERT('goodbyd world', 9, 0, 'cruel ') string;
SELECT INSERT('goodbyd world', 1, 7, 'cruel ') string;
SELECT SUBSTRING('goodbye cruel world', 9, 5);
SELECT SUBSTRING('Please find the substring in this string', 17, 9);

SELECT (37 * 59) / (78 - (8 * 6));
SELECT MOD(10, 4);
SELECT MOD(22.75, 5);
SELECT POW(2, 8);
SELECT POW(2,10) KB, POW(2,20) MB, POW(2,30) GB, POW(2,40) TB;
SELECT CEIL(72.445), FLOOR(72.445);
SELECT ROUND(72.49999), ROUND(72.5), ROUND(72.500001);
SELECT ROUND(17, -1), TRUNCATE(17, -1);
SELECT TRUNCATE(72.0909, 1), TRUNCATE(72.0909, 2), TRUNCATE(72.0909, 3);
SELECT ROUND(-25.76823, 2), SIGN(-25.76823), abs(-25.76823);

SELECT account_id, SIGN(balance), ABS(balance) FROM account;

SELECT @@global.timezone, @@session.time_zone;
SELECT CAST('2020-06-01 13:52:00' AS DATETIME);
SELECT CAST('2020-06-01 13:52:00' AS DATE), CAST('2020-06-01 13:52:00' AS TIME);
SELECT EXTRACT(MONTH FROM CURRENT_DATE();

--   - Grouping and Aggregates
SELECT customer_id FROM rental GROUP BY customer_id;
SELECT customer_id, COUNT(*) FROM rental GROUP BY customer_id;
SELECT customer_id, COUNT(*) FROM rental GROUP BY customer_id ORDER BY 2 DESC;
SELECT customer_id, COUNT(*) FROM rental GROUP BY customer_id HAVING COUNT(*) >= 40 ORDER BY 2 DESC;
SELECT customer_id, MAX(amount) max_amt, MIN(amount) min_amt, AVG(amount) avg_amt, SUM(amount) tot_amt, COUNT(*) num_payments FROM payment GROUP BY customer_id;
SELECT MAX(DATEDIFF(return_date, rental_date)) FROM rental;
SELECT actor_id, COUNT(*) FROM film_actor GROUP BY actor_id;
SELECT fa.actor_id, f.rating, COUNT(*) FROM film_actor fa INNER JOIN film f ON fa.film_id = f.film_id GROUP BY fa.actor_Id, f.rating WITH ROLLUP ORDER BY 1,2;
SELECT customer_id, COUNT(*), SUM(amount) from payment GROUP BY customer_id having COUNT(*) >= 40;

-- 2020-05-28
-- - Querying multiple tables
SELECT c.first_name, c.last_name, a.address FROM customer c, address a WHERE c.address_id = a.address_id;	-- Older Join syntax

--   - ANSI SQL (version SQL92 )
SELECT c.first_name, c.last_name, a.address FROM customer c JOIN address a;	-- Cartesian Product: Every permutation of joined tables
SELECT c.first_name, c.last_name, a.address FROM customer c CROSS JOIN address a;	-- Cartesian Product: Every permutation of joined tables

SELECT c.first_name, c.last_name, a.address FROM customer c JOIN address a ON c.address_id = a.address_id;	-- Inner Join implicitly
SELECT c.first_name, c.last_name, a.address FROM customer c INNER JOIN address a ON c.address_id = a.address_id;	-- Inner Join explicitly
SELECT c.first_name, c.last_name, a.address FROM customer c INNER JOIN address a USING (address_id);	-- Inner Join with USING when field name is same

SELECT c.first_name, c.last_name, addr.address, addr.city FROM customer c
 INNER JOIN (SELECT a.address_id, a.address, ct.city FROM address a INNER JOIN city ct ON a.city_id = ct.city_id WHERE a.district = 'California') addr
  ON c.address_id = addr.address_id;	-- using subqueries as table

SELECT f.title FROM film f 
 INNER JOIN film_actor fa ON f.film_id = fa.film_id 
 INNER JOIN actor a ON fa.actor_id = a.actor_id
 WHERE ((a.first_name = 'CATE' AND a.last_name = 'MCQUEEN') OR (a.first_name = 'CUBA' AND a.last_name = 'BIRCH')); -- More than two tables
 
SELECT f.title FROM film f
  INNER JOIN film_actor fa1 ON f.film_id = fa1.film_id
  INNER JOIN actor a1 ON fa1.actor_id = a1.actor_id
  INNER JOIN film_actor fa2 ON f.film_id = fa2.film_id
  INNER JOIN actor a2 ON fa2.actor_id = a2.actor_id
 WHERE (a1.first_name = 'CATE' AND a1.last_name = 'MCQUEEN') AND (a2.first_name = 'CUBA' AND a2.last_name = 'BIRCH');	-- using same table twice\
 
SELECT f.title, f_prnt.title prequel FROM film f
  INNER JOIN film f_prnt ON f_prnt.film_id = f.prequel_film_id
 WHERE f.prequel_film_id IS NOT NULL;	-- Self Join

-- - Sets
SELECT 1 num, 'abc' str UNION SELECT 9 num, 'xyz' str;
SELECT 'CUST' typ, c.first_name, c.last_name FROM customer c UNION ALL SELECT 'ACTR' typ, a.first_name, a.last_name FROM actor a;
SELECT 'ACTR' typ, a.first_name, a.last_name FROM actor a UNION ALL SELECT 'ACTR' typ, a.first_name, a.last_name FROM actor a;
SELECT c.first_name, c.last_name  FROM customer c WHERE c.first_name LIKE 'J%' AND c.last_name LIKE 'D%' UNION ALL SELECT a.first_name, a.last_name FROM actor a WHERE a.first_name LIKE 'J%' AND a.last_name LIKE 'D%';

SELECT c.first_name, c.last_name FROM customer c WHERE c.first_name LIKE 'J%' AND c.last_name LIKE 'D%' UNION SELECT a.first_name, a.last_name FROM actor a WHERE a.first_name LIKE 'J%' AND a.last_name LIKE 'D%';