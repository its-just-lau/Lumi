-- Base dedicada para las pruebas de integración. El fixture de pytest borra y
-- recrea el esquema, así que nunca debe apuntar a `lumi` (la base de trabajo).
CREATE DATABASE lumi_test;
