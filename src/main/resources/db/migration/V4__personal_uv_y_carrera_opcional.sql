-- =========================================================
-- V4: permite cuentas de personal de la UV (@uv.mx), que no tienen
--     matrícula ni carrera. Estudiantes y egresados siguen igual.
-- =========================================================

-- Quita los CHECK que mencionan tipo_usuario (el de valores permitidos y el de datos
-- académicos), sin depender del nombre que Postgres les haya puesto.
DO $$
DECLARE r RECORD;
BEGIN
    FOR r IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'usuario'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) LIKE '%tipo_usuario%'
    LOOP
        EXECUTE format('ALTER TABLE usuario DROP CONSTRAINT %I', r.conname);
    END LOOP;
END $$;

ALTER TABLE usuario ALTER COLUMN id_carrera DROP NOT NULL;

ALTER TABLE usuario ADD CONSTRAINT chk_usuario_tipo
    CHECK (tipo_usuario IN ('ESTUDIANTE', 'EGRESADO', 'PERSONAL'));

ALTER TABLE usuario ADD CONSTRAINT chk_usuario_datos_academicos CHECK (
    (tipo_usuario = 'ESTUDIANTE'
        AND matricula IS NOT NULL AND anio_egreso IS NULL AND id_carrera IS NOT NULL)
    OR
    (tipo_usuario = 'EGRESADO'
        AND anio_egreso IS NOT NULL AND anio_egreso > 0 AND id_carrera IS NOT NULL)
    OR
    (tipo_usuario = 'PERSONAL'
        AND matricula IS NULL AND anio_egreso IS NULL)
);
