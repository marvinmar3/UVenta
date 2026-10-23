
-- =========================================================
-- UVenta - V2__datos_iniciales.sql
-- =========================================================

-- Carreras: agrega aquí los nombres oficiales de los
-- programas académicos de la Facultad.

INSERT INTO carrera (nombre)
VALUES
    ('Ingeniería Informática'),('Ingeniería Mecatrónica'), ('Ingeniería en Electrónica y Comunicaciones')
ON CONFLICT (nombre) DO NOTHING;


-- Categorías principales
INSERT INTO categoria (nombre, descripcion)
VALUES
    ('Libros y material académico',
     'Libros, apuntes, calculadoras y material escolar'),

    ('Electrónica y tecnología',
     'Componentes electrónicos, computadoras y accesorios'),

    ('Ropa y accesorios',
     'Prendas, mochilas y accesorios'),

    ('Alimentos',
     'Comida, postres y bebidas'),

    ('Servicios estudiantiles',
     'Asesorías, tutorías y servicios ofrecidos por estudiantes'),

    ('Hogar y artículos generales',
     'Mobiliario, regalos y otros artículos')
ON CONFLICT (nombre) DO NOTHING;


-- Plan de publicación destacada por una semana
INSERT INTO plan_destacado
    (nombre, precio, duracion_dias, prioridad, activo)
VALUES
    ('Destacado semanal', 20.00, 7, 1, TRUE)
ON CONFLICT (nombre) DO NOTHING;
