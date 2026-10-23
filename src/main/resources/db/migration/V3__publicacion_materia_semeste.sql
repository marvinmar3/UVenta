ALTER TABLE publicacion
    ADD COLUMN materia VARCHAR(120),
    ADD COLUMN semestre SMALLINT
        CHECK (semestre BETWEEN 1 AND 12);

CREATE INDEX idx_publicacion_materia
    ON publicacion (materia);