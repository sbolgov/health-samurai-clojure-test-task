/*
psql <your_db> -U <superuser>
CREATE USER healthsamuraitest WITH CREATEDB;
(exit)

psql <your_db> -U healthsamuraitest
CREATE DATABASE patients_db;
(exit)

psql patients_db -U healthsamuraitest
*/

CREATE TABLE patients (
    patient_id uuid default gen_random_uuid(),
    first_name text not null,
    middle_name text,
    last_name text not null,
    gender text not null,
    address1 text not null,
    address2 text,
    city text not null,
    state text not null,
    zip text not null,
    country text not null,
    policy text unique not null,
    PRIMARY KEY (patient_id)
);
