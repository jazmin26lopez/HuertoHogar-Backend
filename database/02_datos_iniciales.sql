--------------------------------------------------------------------------------
-- HUERTOHOGAR API - 02 Datos iniciales
-- Categorias y productos: los mismos del frontend (src/datos/productos.js), con
-- origen, descripcion y stock tomados del taller de base de datos.
-- Descuentos de oferta: los mismos de src/funciones/ofertas.js.
--
-- Los USUARIOS no se cargan aqui: sus contrasenas deben guardarse con hash BCrypt,
-- asi que la aplicacion Spring los crea al arrancar (administrador, vendedor y cliente).
--------------------------------------------------------------------------------

INSERT INTO categoria (nombre, descripcion) VALUES
('Frutas Frescas', 'Frutas cultivadas y cosechadas en su punto óptimo de madurez.');
INSERT INTO categoria (nombre, descripcion) VALUES
('Verduras Orgánicas', 'Verduras cultivadas sin pesticidas ni químicos.');
INSERT INTO categoria (nombre, descripcion) VALUES
('Productos Orgánicos', 'Productos elaborados con ingredientes naturales y procesamiento responsable.');
INSERT INTO categoria (nombre, descripcion) VALUES
('Productos Lácteos', 'Leches, yogures y derivados provenientes de granjas locales.');

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('FR001', 'Manzanas Fuji', (SELECT categoria_id FROM categoria WHERE nombre = 'Frutas Frescas'),
 1200, 150, 'kilo', 'Valle del Maule',
 'Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule.', '/imagenes/manzanas.jpg', 20);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('FR002', 'Naranjas Valencia', (SELECT categoria_id FROM categoria WHERE nombre = 'Frutas Frescas'),
 1000, 200, 'kilo', 'Chile',
 'Jugosas y ricas en vitamina C, ideales para zumos frescos.', '/imagenes/naranjas.jpg', 0);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('FR003', 'Plátanos Cavendish', (SELECT categoria_id FROM categoria WHERE nombre = 'Frutas Frescas'),
 800, 250, 'kilo', 'Importado',
 'Plátanos maduros y dulces, ricos en potasio y vitaminas.', '/imagenes/platanos.jpg', 0);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('VR001', 'Zanahorias Orgánicas', (SELECT categoria_id FROM categoria WHERE nombre = 'Verduras Orgánicas'),
 900, 100, 'kilo', 'Región de O''Higgins',
 'Zanahorias crujientes cultivadas sin pesticidas.', '/imagenes/zanahorias.jpg', 15);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('VR002', 'Espinacas Frescas', (SELECT categoria_id FROM categoria WHERE nombre = 'Verduras Orgánicas'),
 700, 80, 'bolsa 500g', 'Chile',
 'Espinacas frescas cultivadas bajo prácticas orgánicas.', '/imagenes/espinacas.jpg', 0);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('VR003', 'Pimientos Tricolores', (SELECT categoria_id FROM categoria WHERE nombre = 'Verduras Orgánicas'),
 1500, 120, 'kilo', 'Chile',
 'Pimientos rojos, amarillos y verdes, ricos en antioxidantes.', '/imagenes/pimientos.jpg', 0);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('PO001', 'Miel Orgánica', (SELECT categoria_id FROM categoria WHERE nombre = 'Productos Orgánicos'),
 5000, 50, 'frasco 500g', 'Chile',
 'Miel pura y orgánica producida por apicultores locales.', '/imagenes/miel.jpg', 10);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('PO003', 'Quinua Orgánica', (SELECT categoria_id FROM categoria WHERE nombre = 'Productos Orgánicos'),
 3200, 60, 'bolsa 500g', 'Chile',
 'Quinua orgánica, fuente de proteína vegetal.', '/imagenes/quinua.jpg', 0);

INSERT INTO producto (codigo, nombre, categoria_id, precio, stock, unidad_medida, origen, descripcion, imagen, descuento_oferta) VALUES
('PL001', 'Leche Entera', (SELECT categoria_id FROM categoria WHERE nombre = 'Productos Lácteos'),
 1100, 90, 'litro', 'Región de Los Lagos',
 'Leche entera proveniente de granjas locales.', '/imagenes/leche.jpg', 12);

COMMIT;

-- Comprobacion rapida: debe dar 4 categorias y 9 productos
SELECT (SELECT COUNT(*) FROM categoria) AS categorias,
       (SELECT COUNT(*) FROM producto)  AS productos
FROM   dual;
