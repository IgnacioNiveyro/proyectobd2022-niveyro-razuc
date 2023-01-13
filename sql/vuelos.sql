CREATE DATABASE vuelos;

USE vuelos;

#Creacion de tablas para las Entidades.

CREATE TABLE ubicaciones(
    pais VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    ciudad VARCHAR(20) NOT NULL,
    huso TINYINT NOT NULL,

    CONSTRAINT pk_ubicaciones
    PRIMARY KEY(pais, estado, ciudad)
) ENGINE=InnoDB;

CREATE TABLE modelos_avion(
    modelo VARCHAR(20) NOT NULL,
    fabricante VARCHAR(20) NOT NULL,
    cabinas INT UNSIGNED NOT NULL,
    cant_asientos INT UNSIGNED NOT NULL,

    CONSTRAINT pk_modelos_vuelos
    PRIMARY KEY(modelo)
) ENGINE=InnoDB;

CREATE TABLE comodidades(
    codigo INT UNSIGNED NOT NULL,
    descripcion TEXT NOT NULL,

    CONSTRAINT pk_comodidades
    PRIMARY KEY(codigo)
) ENGINE=InnoDB;

CREATE TABLE clases(
    nombre VARCHAR(20) NOT NULL,
    porcentaje DECIMAL(2,2) UNSIGNED NOT NULL,

    CONSTRAINT pk_clases
    PRIMARY KEY(nombre)
) ENGINE=InnoDB;

CREATE TABLE pasajeros(
    doc_tipo VARCHAR(3) NOT NULL,
    doc_nro INT(8) UNSIGNED NOT NULL,
    apellido VARCHAR(20) NOT NULL,
    nombre VARCHAR(20) NOT NULL,
    direccion VARCHAR(40) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    nacionalidad VARCHAR(20) NOT NULL,

    CONSTRAINT pk_pasajeros
    PRIMARY KEY(doc_tipo,doc_nro)
) ENGINE=InnoDB;

CREATE TABLE empleados(
    legajo INT UNSIGNED NOT NULL,
    password VARCHAR(32) NOT NULL,
    doc_tipo VARCHAR(3) NOT NULL,
    doc_nro INT(8) UNSIGNED NOT NULL,
    apellido VARCHAR(20) NOT NULL,
    nombre VARCHAR(20) NOT NULL,
    direccion VARCHAR(40) NOT NULL,
    telefono VARCHAR(15) NOT NULL,

    CONSTRAINT pk_empleados
    PRIMARY KEY(legajo)
) ENGINE=InnoDB;

CREATE TABLE aeropuertos(
    codigo VARCHAR(10) NOT NULL, 
    nombre VARCHAR(40) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    direccion VARCHAR(30) NOT NULL,
    pais VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    ciudad VARCHAR(20) NOT NULL,

    CONSTRAINT pk_aeropuertos
    PRIMARY KEY (codigo),

    CONSTRAINT FK_aeropuertos_ubicaciones
    FOREIGN KEY(pais,estado,ciudad) REFERENCES ubicaciones(pais,estado,ciudad)

) ENGINE=InnoDB;

CREATE TABLE reservas(
    numero INT UNSIGNED NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    vencimiento DATE NOT NULL,
    estado VARCHAR(15) NOT NULL,
    doc_tipo VARCHAR(3) NOT NULL,
    doc_nro INT(8) UNSIGNED NOT NULL,
    legajo INT UNSIGNED NOT NULL,

    CONSTRAINT pk_reservas
    PRIMARY KEY(numero),

    CONSTRAINT FK_reservas_pasajeros
    FOREIGN KEY(doc_tipo, doc_nro) REFERENCES pasajeros(doc_tipo,doc_nro),

    CONSTRAINT FK_reservas_empleados
    FOREIGN KEY(legajo) REFERENCES empleados(legajo)

) ENGINE=InnoDB;

CREATE TABLE vuelos_programados (
    numero VARCHAR(10) NOT NULL,
    aeropuerto_salida VARCHAR(10) NOT NULL,
    aeropuerto_llegada VARCHAR(10) NOT NULL,

    CONSTRAINT pk_vuelos_programados
    PRIMARY KEY(numero),

    CONSTRAINT FK_vuelos_programados_salida
    FOREIGN KEY (aeropuerto_salida) REFERENCES aeropuertos(codigo),

    CONSTRAINT FK_vuelos_programados_llegada 
    FOREIGN KEY (aeropuerto_llegada) REFERENCES aeropuertos(codigo)
) ENGINE=InnoDB;

CREATE TABLE salidas(
    vuelo VARCHAR(10) NOT NULL,
    dia ENUM('Do','Lu','Ma','Mi','Ju','Vi','Sa') NOT NULL,
    hora_sale TIME(4) NOT NULL,
    hora_llega TIME(4) NOT NULL,
    modelo_avion VARCHAR(20) NOT NULL,

    CONSTRAINT pk_salidas
    PRIMARY KEY(vuelo,dia),

    CONSTRAINT FK_salidas_vuelos_programados
    FOREIGN KEY(vuelo) REFERENCES vuelos_programados(numero),

    CONSTRAINT FK_salidas_modelos_avion
    FOREIGN KEY(modelo_avion) REFERENCES modelos_avion(modelo)
) ENGINE=InnoDB;

CREATE TABLE instancias_vuelo(
    vuelo VARCHAR(10) NOT NULL,
    fecha DATE NOT NULL,
    dia ENUM('Do','Lu','Ma','Mi','Ju','Vi','Sa') NOT NULL,
    estado VARCHAR(15) NULL,

    CONSTRAINT pk_instancias_vuelo
    PRIMARY KEY(vuelo,fecha),

    CONSTRAINT FK_instancias_vuelos_salidas
    FOREIGN KEY(vuelo,dia) REFERENCES salidas(vuelo,dia)
) ENGINE=InnoDB;


#---------------------
#Creacion de tablas para las Relaciones.

CREATE TABLE brinda(
    clase VARCHAR(20) NOT NULL,
    vuelo VARCHAR(10) NOT NULL,
    dia ENUM('Do','Lu','Ma','Mi','Ju','Vi','Sa') NOT NULL,
    precio DEC(7,2) UNSIGNED NOT NULL,
    cant_asientos INT UNSIGNED NOT NULL,

    CONSTRAINT pk_brinda
    PRIMARY KEY(vuelo,dia,clase),

    CONSTRAINT FK_brinda_salidas
    FOREIGN KEY(vuelo,dia) REFERENCES salidas(vuelo,dia),

    CONSTRAINT FK_brinda_clases
    FOREIGN KEY(clase) REFERENCES clases(nombre)
) ENGINE=InnoDB;

CREATE TABLE posee(
    comodidad INT UNSIGNED NOT NULL,
    clase VARCHAR(20) NOT NULL,
    
    CONSTRAINT pk_posee
    PRIMARY KEY(comodidad,clase),
    
    CONSTRAINT FK_posee_clases
    FOREIGN KEY(clase) REFERENCES clases(nombre),

    CONSTRAINT FK_posee_comodidades
    FOREIGN KEY(comodidad) REFERENCES comodidades(codigo)
) ENGINE=InnoDB;

CREATE TABLE reserva_vuelo_clase(
    numero INT UNSIGNED NOT NULL,
    vuelo VARCHAR(10) NOT NULL,
    fecha_vuelo DATE NOT NULL,
    clase VARCHAR(20) NOT NULL,

    CONSTRAINT pk_reserva_vuelo_clase
    PRIMARY KEY(numero,vuelo,fecha_vuelo),

    CONSTRAINT FK_reserva_vuelo_clase_reservas
    FOREIGN KEY(numero) REFERENCES reservas(numero),

    CONSTRAINT FK_reserva_vuelo_clase_instancias_vuelo
    FOREIGN KEY(vuelo,fecha_vuelo) REFERENCES instancias_vuelo(vuelo,fecha),

    CONSTRAINT FK_reserva_vuelo_clase_clases
    FOREIGN KEY(clase) REFERENCES clases(nombre)
) ENGINE=InnoDB;

CREATE TABLE asientos_reservados( 
    cantidad INT UNSIGNED NOT NULL,    
    vuelo VARCHAR(10) NOT NULL,
    fecha DATE NOT NULL,
    clase VARCHAR(20) NOT NULL,

    CONSTRAINT pk_asientos_reservados
    PRIMARY KEY(vuelo,fecha,clase),

    CONSTRAINT FK_asientos_reservados_instancias_vuelo
    FOREIGN KEY(vuelo,fecha) REFERENCES instancias_vuelo(vuelo,fecha),

    CONSTRAINT FK_asientos_reservados_clases
    FOREIGN KEY(clase) REFERENCES clases(nombre)
) ENGINE=InnoDB;

# Creación de vistas 

CREATE VIEW vuelos_disponibles as 
   	SELECT DISTINCT 
	s.vuelo AS "Nro_vuelo", s.modelo_avion AS "Modelo", s.dia AS "dia_sale", s.hora_sale AS "hora_sale",
	TIMEDIFF(TIMEDIFF(s.hora_llega, MAKETIME(uLlega.huso,0,0)) , TIMEDIFF(s.hora_sale, MAKETIME(uSale.huso,0,0))) as "tiempo_estimado",
	iv.fecha AS "Fecha", s.hora_llega AS "hora_llega", arSale.codigo AS "codigo_aero_sale",arSale.nombre AS "nombre_aero_sale",
	arSale.ciudad AS "ciudad_sale", arSale.estado AS "estado_sale",arSale.pais AS "pais_sale",arLlega.codigo AS "codigo_aero_llega", arLlega.nombre AS "nombre_aero_llega",
	arLlega.ciudad AS "ciudad_llega", arLlega.estado AS "estado_llega", arLlega.pais AS "pais_llega", b.precio AS "Precio", c.nombre AS "clase", round(((b.cant_asientos + c.porcentaje*b.cant_asientos)-ar.cantidad)) AS "asientos_disponibles"

FROM	brinda b JOIN clases c ON b.clase = c.nombre JOIN asientos_reservados ar ON c.nombre = ar.clase 
		JOIN salidas s ON b.vuelo=s.vuelo AND b.dia = s.dia JOIN instancias_vuelo iv ON s.vuelo = iv.vuelo AND s.dia = iv.dia AND iv.vuelo = ar.vuelo AND iv.fecha = ar.fecha JOIN vuelos_programados vp ON vp.numero=s.vuelo JOIN
		aeropuertos arSale ON arSale.codigo = vp.aeropuerto_salida JOIN ubicaciones uSale ON arSale.pais = uSale.pais AND arSale.estado = uSale.estado AND arSale.ciudad = uSale.ciudad
		JOIN aeropuertos arLlega ON arLlega.codigo=vp.aeropuerto_llegada JOIN ubicaciones uLlega ON arLlega.pais = uLlega.pais AND arLlega.estado = uLlega.estado AND arLlega.ciudad = uLlega.ciudad;

#-------------------------------------------------------------------------

delimiter !
CREATE PROCEDURE reservaSoloIda (IN fechaSoloIda DATE, IN doc_tipo VARCHAR(45), IN doc_nro INT, IN legajo INT,IN vueloSoloIda VARCHAR(10), IN claseSoloIda VARCHAR(20))
BEGIN

	DECLARE estado_reserva VARCHAR(15) DEFAULT 'En Espera';
	DECLARE cant_de_reservas INT;
	DECLARE vencimiento DATE;
	DECLARE asientos_disp INT;
	DECLARE asientosIda INT;

	
	DECLARE codigo_SQL CHAR(5) DEFAULT '00000';
	DECLARE codigo_MYSQL INT DEFAULT 0;
	DECLARE mensaje_error TEXT;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
		BEGIN
			GET DIAGNOSTICS CONDITION 1
				codigo_MYSQL = MYSQL_ERRNO,
				codigo_SQL = RETURNED_SQLSTATE,
				mensaje_error = MESSAGE_TEXT;
			SELECT 'SQLEXCEPTION: transaccion abortada' AS resultado, codigo_MYSQL, codigo_SQL, mensaje_error;
			ROLLBACK;
		END;	

	START TRANSACTION;

		
		IF EXISTS (SELECT * FROM instancias_vuelo AS iv WHERE (iv.vuelo = vueloSoloIda) AND (iv.fecha = fechaSoloIda)) THEN
			IF EXISTS (SELECT * FROM brinda AS b WHERE (b.clase = claseSoloIda) AND (b.vuelo = vueloSoloIda)) THEN
				IF EXISTS (SELECT * FROM pasajeros AS p WHERE (p.doc_tipo = doc_tipo) AND (p.doc_nro = doc_nro)) THEN
					IF EXISTS (SELECT * FROM empleados AS e WHERE (e.legajo = legajo)) THEN


						SELECT cantidad INTO cant_de_reservas
						FROM asientos_reservados AS a_reservados
						WHERE ((a_reservados.clase = claseSoloIda) AND
								(a_reservados.fecha = fechaSoloIda) AND
								(a_reservados.vuelo = vueloSoloIda))
						FOR UPDATE;
						
						
						SELECT asientos_disponibles INTO asientos_disp
														FROM vuelos_disponibles AS vuelos_disp
														WHERE ((vuelos_disp.nro_vuelo = vueloSoloIda) AND 
																(vuelos_disp.fecha = fechaSoloIda) AND 
																(vuelos_disp.clase = claseSoloIda));

						IF (asientos_disp > 0) THEN			

							SELECT cant_asientos INTO asientosIda
							FROM brinda NATURAL JOIN instancias_vuelo
							WHERE vuelo=vueloSoloIda AND clase = claseSoloIda AND Fecha=fechaSoloIda;

							IF (cant_de_reservas < asientosIda) THEN
								SET estado_reserva = 'Confirmada';
							ELSE
								SET estado_reserva ='En Espera';		
							END IF;							


							SET vencimiento = (SELECT DATE_SUB(fechaSoloIda, INTERVAL 15 DAY));
							
							INSERT INTO reservas (fecha, vencimiento, estado, doc_tipo, doc_nro, legajo) 
									VALUES (CURDATE(), vencimiento, estado_reserva, doc_tipo, doc_nro, legajo);

							INSERT INTO reserva_vuelo_clase (numero, vuelo, fecha_vuelo, clase)
									VALUES (LAST_INSERT_ID(), vueloSoloIda, fechaSoloIda, claseSoloIda);							
							
							UPDATE asientos_reservados AS a_reservados
							SET cantidad = cantidad + 1
							WHERE ((a_reservados.clase = claseSoloIda) AND
									(a_reservados.fecha = fechaSoloIda) AND
									(a_reservados.vuelo = vueloSoloIda));


							SELECT 'Reserva exitosa' AS resultado, 
									LAST_INSERT_ID() AS numero_reserva;

						ELSE
							SELECT 'ERROR: no hay disponibilidad para ese vuelo y clase' AS resultado;
						END IF;		

					ELSE
						SELECT 'ERROR: no existe un empleado con ese legajo' AS resultado;
					END IF;	
				ELSE
					SELECT 'ERROR: no existe un pasajero con ese tipo y numero de DNI' AS resultado;
				END IF;	
			ELSE
				SELECT 'ERROR: no existe la clase en ese vuelo' AS resultado;
			END IF;	
		ELSE
			SELECT 'ERROR: no existe un numero de vuelo en esa fecha' AS resultado;
		END IF;

	COMMIT;
END;!

#-------------------------------------------------------------------------

CREATE PROCEDURE reservaIdaVuelta (IN vuelo_ida VARCHAR(10), IN fecha_ida DATE, IN clase_ida VARCHAR(20), 
									IN vuelo_vuelta VARCHAR(10), IN fecha_vuelta DATE, IN clase_vuelta VARCHAR(20),
									IN doc_tipo VARCHAR(45), IN doc_nro INT, IN legajo INT)
BEGIN

	DECLARE estado_reserva VARCHAR(15) DEFAULT 'En Espera';
	DECLARE cant_de_reservas_ida INT;
	DECLARE cant_de_reservas_vuelta INT;
	DECLARE asientos_disp_ida INT;
	DECLARE asientos_disp_vuelta INT;
	DECLARE vencimiento_ida DATE;
	DECLARE asientosIda INT;
	DECLARE asientosVuelta INT;

	
	DECLARE codigo_SQL CHAR(5) DEFAULT '00000';
	DECLARE codigo_MYSQL INT DEFAULT 0;
	DECLARE mensaje_error TEXT;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
		BEGIN
			GET DIAGNOSTICS CONDITION 1
				codigo_MYSQL = MYSQL_ERRNO,
				codigo_SQL = RETURNED_SQLSTATE,
				mensaje_error = MESSAGE_TEXT;
			SELECT 'SQLEXCEPTION: transaccion abortada' AS resultado, codigo_MYSQL, codigo_SQL, mensaje_error;
			ROLLBACK;
		END;	

	START TRANSACTION;

		
		IF EXISTS (SELECT * FROM instancias_vuelo AS iv WHERE (iv.vuelo = vuelo_ida) AND (iv.fecha = fecha_ida)) AND
		   EXISTS (SELECT * FROM instancias_vuelo AS iv WHERE (iv.vuelo = vuelo_vuelta) AND (iv.fecha = fecha_vuelta)) THEN

			IF EXISTS (SELECT * FROM brinda AS b WHERE (b.clase = clase_ida) AND (b.vuelo = vuelo_ida)) AND
			   EXISTS (SELECT * FROM brinda AS b WHERE (b.clase = clase_vuelta) AND (b.vuelo = vuelo_vuelta)) THEN

				IF EXISTS (SELECT * FROM pasajeros AS p WHERE (p.doc_tipo = doc_tipo) AND (p.doc_nro = doc_nro)) THEN

					IF EXISTS (SELECT * FROM empleados AS e WHERE (e.legajo = legajo)) THEN


						
						SELECT cantidad INTO cant_de_reservas_ida
						FROM asientos_reservados AS a_reservados
						WHERE ((a_reservados.clase = clase_ida) AND
								(a_reservados.fecha = fecha_ida) AND
								(a_reservados.vuelo = vuelo_ida))
						FOR UPDATE;

						
						SELECT cantidad INTO cant_de_reservas_vuelta
						FROM asientos_reservados AS a_reservados
						WHERE ((a_reservados.clase = clase_vuelta) AND
								(a_reservados.fecha = fecha_vuelta) AND
								(a_reservados.vuelo = vuelo_vuelta))
						FOR UPDATE;
						

						SELECT asientos_disponibles INTO asientos_disp_ida
						FROM vuelos_disponibles AS vuelos_disp
						WHERE ((vuelos_disp.nro_vuelo = vuelo_ida) AND 
								(vuelos_disp.fecha = fecha_ida) AND 
								(vuelos_disp.clase = clase_ida));

						
						SELECT asientos_disponibles INTO asientos_disp_vuelta
						FROM vuelos_disponibles AS vuelos_disp
						WHERE ((vuelos_disp.nro_vuelo = vuelo_vuelta) AND 
								(vuelos_disp.fecha = fecha_vuelta) AND 
								(vuelos_disp.clase = clase_vuelta));



						IF (asientos_disp_ida > 0) THEN	
							IF (asientos_disp_vuelta > 0) THEN	
							
								SELECT cant_asientos INTO asientosIda
								FROM brinda NATURAL JOIN instancias_vuelo
								WHERE vuelo=vuelo_ida AND clase = clase_ida AND Fecha=fecha_ida;

								SELECT cant_asientos INTO asientosVuelta
								FROM brinda NATURAL JOIN instancias_vuelo
								WHERE vuelo=vuelo_vuelta AND clase = clase_vuelta AND Fecha=fecha_vuelta;


								IF (cant_de_reservas_ida < asientosIda) AND (cant_de_reservas_vuelta < asientosVuelta) THEN
									SET estado_reserva = 'Confirmada';
								ELSE
									SET estado_reserva ='En Espera';		
								END IF;

								SET vencimiento_ida = (SELECT DATE_SUB(fecha_ida, INTERVAL 15 DAY));
								
								INSERT INTO reservas (fecha, vencimiento, estado, doc_tipo, doc_nro, legajo) 
										VALUES (CURDATE(), vencimiento_ida, estado_reserva, doc_tipo, doc_nro, legajo);

								INSERT INTO reserva_vuelo_clase (numero, vuelo, fecha_vuelo, clase)
										VALUES (LAST_INSERT_ID(), vuelo_ida, fecha_ida, clase_ida);

								INSERT INTO reserva_vuelo_clase (numero, vuelo, fecha_vuelo, clase)
										VALUES (LAST_INSERT_ID(), vuelo_vuelta, fecha_vuelta, clase_vuelta);


								
								UPDATE asientos_reservados AS a_reservados
								SET cantidad = cantidad + 1
								WHERE ((a_reservados.clase = clase_ida) AND
										(a_reservados.fecha = fecha_ida) AND
										(a_reservados.vuelo = vuelo_ida));

								UPDATE asientos_reservados AS a_reservados
								SET cantidad = cantidad + 1
								WHERE ((a_reservados.clase = clase_vuelta) AND
										(a_reservados.fecha = fecha_vuelta) AND
										(a_reservados.vuelo = vuelo_vuelta));


								SELECT 'Reserva exitosa' AS resultado, 
										LAST_INSERT_ID() AS numero_reserva;


							ELSE
								SELECT 'ERROR: no hay disponibilidad para ese vuelo de vuelta y clase' AS resultado;
							END IF;
						ELSE
							SELECT 'ERROR: no hay disponibilidad para ese vuelo de ida y clase' AS resultado;
						END IF;	
					ELSE
						SELECT 'ERROR: no existe un empleado con ese legajo' AS resultado;
					END IF;	
				ELSE
					SELECT 'ERROR: no existe un pasajero con ese tipo y numero de DNI' AS resultado;
				END IF;	
			ELSE
				SELECT 'ERROR: no existe la clase en ese vuelo' AS resultado;
			END IF;	
		ELSE
			SELECT 'ERROR: no existe un numero de vuelo en esa fecha' AS resultado;
		END IF;

	COMMIT;
END;!



#-------------------------------------------------------------------------

CREATE TRIGGER inicializar_asientos_reservados
AFTER INSERT ON instancias_vuelo
FOR EACH ROW
BEGIN
	DECLARE fin BOOLEAN DEFAULT FALSE; 
	DECLARE nombre_clase VARCHAR(20); 

	DECLARE C CURSOR FOR SELECT clase FROM brinda WHERE (brinda.vuelo = NEW.vuelo AND brinda.dia = NEW.dia);

	DECLARE CONTINUE HANDLER FOR NOT FOUND SET fin = TRUE;

	OPEN C; 

	FETCH C INTO nombre_clase;

	WHILE NOT fin DO

		INSERT INTO asientos_reservados (vuelo, fecha, clase, cantidad)
				VALUES (NEW.vuelo, NEW.fecha, nombre_clase, 0);

		FETCH C INTO nombre_clase;
	END WHILE;

END;!

DELIMITER ; 

#-------------------------------------------------------------------------

# Creación de usuarios y otorgamiento de privilegios

   CREATE USER 'admin'@'localhost'  IDENTIFIED BY 'admin';

# luego le otorgo privilegios utilizando solo la sentencia GRANT

    GRANT ALL PRIVILEGES ON vuelos.* TO 'admin'@'localhost' WITH GRANT OPTION;

# El usuario 'admin' tiene acceso total a todas las tablas de 
# la B.D. vuelos y puede crear nuevos usuarios y otorgar privilegios.

#-------------------------------------------------------------------------

    CREATE USER 'empleado'@'%' IDENTIFIED BY 'empleado'; 

# el usuario 'empleado' con password 'empleado' puede conectarse solo desde localhost

# Luego le otorgo privilegios con GRANT

	GRANT SELECT ON vuelos.* TO 'empleado'@'%';
	GRANT INSERT, DELETE, UPDATE ON vuelos.reservas TO 'empleado'@'%';
	GRANT INSERT, DELETE, UPDATE ON vuelos.pasajeros TO 'empleado'@'%';
	GRANT INSERT, DELETE, UPDATE ON vuelos.reserva_vuelo_clase TO 'empleado'@'%';
	
#-------------------------------------------------------------------------

    CREATE USER 'cliente'@'%' IDENTIFIED BY 'cliente'; 

# Luego le otorgo privilegios con GRANT

    GRANT SELECT ON vuelos.vuelos_disponibles TO 'cliente'@'%';

# el usuario 'cliente' solo puede acceder a la tabla (vista) vuelos_disponibles
# con permiso para selecionar  


#-------------------------------------------------------------------------

GRANT EXECUTE ON PROCEDURE vuelos.reservaSoloIda TO 'empleado'@'%';
GRANT EXECUTE ON PROCEDURE vuelos.reservaIdaVuelta TO 'empleado'@'%';

#-------------------------------------------------------------------------