-- V5: cada categoría guarda su icono (clase de Bootstrap Icons) para pintarlo en la página principal.
ALTER TABLE categoria ADD COLUMN icono VARCHAR(40) NOT NULL DEFAULT 'bi-grid';

UPDATE categoria SET icono = CASE nombre
    WHEN 'Libros y material académico'  THEN 'bi-book'
    WHEN 'Electrónica y tecnología'     THEN 'bi-cpu'
    WHEN 'Ropa y accesorios'            THEN 'bi-handbag'
    WHEN 'Alimentos'                    THEN 'bi-cup-hot'
    WHEN 'Servicios estudiantiles'      THEN 'bi-mortarboard'
    WHEN 'Hogar y artículos generales'  THEN 'bi-house-door'
    ELSE 'bi-grid'
END;
