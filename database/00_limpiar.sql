--------------------------------------------------------------------------------
-- HUERTOHOGAR API - 00 Limpieza
-- Usuario Oracle: HUERTO_API (esquema propio de la Evaluacion 3; NO es el del taller)
--
-- Borra las tablas de la API si existen, para poder volver a crearlas desde cero.
-- Es seguro correrlo aunque no exista nada.
--------------------------------------------------------------------------------
BEGIN
    FOR t IN (
        SELECT table_name
        FROM   user_tables
        WHERE  table_name IN ('DETALLE_BOLETA', 'BOLETA', 'PRODUCTO', 'CATEGORIA', 'USUARIO')
    ) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
    END LOOP;
END;
/
