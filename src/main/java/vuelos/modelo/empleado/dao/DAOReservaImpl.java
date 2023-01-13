package vuelos.modelo.empleado.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vuelos.modelo.empleado.beans.AeropuertoBean;
import vuelos.modelo.empleado.beans.AeropuertoBeanImpl;
import vuelos.modelo.empleado.beans.DetalleVueloBean;
import vuelos.modelo.empleado.beans.DetalleVueloBeanImpl;
import vuelos.modelo.empleado.beans.EmpleadoBean;
import vuelos.modelo.empleado.beans.InstanciaVueloBean;
import vuelos.modelo.empleado.beans.InstanciaVueloBeanImpl;
import vuelos.modelo.empleado.beans.InstanciaVueloClaseBean;
import vuelos.modelo.empleado.beans.InstanciaVueloClaseBeanImpl;
import vuelos.modelo.empleado.beans.PasajeroBean;
import vuelos.modelo.empleado.beans.ReservaBean;
import vuelos.modelo.empleado.beans.ReservaBeanImpl;
import vuelos.modelo.empleado.beans.UbicacionesBean;
import vuelos.modelo.empleado.beans.UbicacionesBeanImpl;
import vuelos.modelo.empleado.dao.datosprueba.DAOReservaDatosPrueba;
import vuelos.utils.Fechas;
public class DAOReservaImpl implements DAOReserva {

	private static Logger logger = LoggerFactory.getLogger(DAOReservaImpl.class);
	
	//conexión para acceder a la Base de Datos
	private Connection conexion;
	
	public DAOReservaImpl(Connection conexion) {
		this.conexion = conexion;
	}
		
	
	@Override
	public int reservarSoloIda(PasajeroBean pasajero, 
							   InstanciaVueloBean vuelo, 
							   DetalleVueloBean detalleVuelo,
							   EmpleadoBean empleado) throws Exception {
		logger.info("Realiza la reserva de solo ida con pasajero {}", pasajero.getNroDocumento());
		
		/**
		 * TODO (parte 2) Realizar una reserva de ida solamente llamando al Stored Procedure (S.P.) correspondiente. 
		 *      Si la reserva tuvo exito deberá retornar el número de reserva. Si la reserva no tuvo éxito o 
		 *      falla el S.P. deberá propagar un mensaje de error explicativo dentro de una excepción.
		 *      La demás excepciones generadas automáticamente por algun otro error simplemente se propagan.
		 *      
		 *      Nota: para acceder a la B.D. utilice la propiedad "conexion" que ya tiene una conexión
		 *      establecida con el servidor de B.D. (inicializada en el constructor DAOReservaImpl(...)).
		 *		
		 * 
		 * @throws Exception. Deberá propagar la excepción si ocurre alguna. Puede capturarla para loguear los errores
		 *		   pero luego deberá propagarla para que el controlador se encargue de manejarla.
		 *
		 * try (CallableStatement cstmt = conexion.prepareCall("CALL PROCEDURE reservaSoloIda(?, ?, ?, ?, ?, ?, ?)"))
		 * {
		 *  ...
		 * }
		 * catch (SQLException ex){
		 * 			logger.debug("Error al consultar la BD. SQLException: {}. SQLState: {}. VendorError: {}.", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		 *  		throw ex;
		 * } 
		 */
		int numero_reserva=0;
		String call = "CALL reservaSoloIda(?, ?, ?, ?, ?, ?)";
		
		try {
			CallableStatement cstmt = conexion.prepareCall(call);
			
			cstmt.setDate(1, Fechas.convertirDateADateSQL(vuelo.getFechaVuelo()));
			cstmt.setString(2, pasajero.getTipoDocumento());
			cstmt.setInt(3, pasajero.getNroDocumento());
			cstmt.setInt(4, empleado.getLegajo());
			cstmt.setString(5, vuelo.getNroVuelo());
			cstmt.setString(6, detalleVuelo.getClase());
			
			ResultSet result_reserva;
			
			result_reserva = cstmt.executeQuery();
			
			if (result_reserva.next()) {
				String resultado = result_reserva.getString("resultado");
				logger.debug("Resultado reserva: {}.", resultado);
				
				if(resultado.equals("Reserva exitosa")) {
					numero_reserva = result_reserva.getInt("numero_reserva");
				}
				else {
					throw new Exception(result_reserva.getString("resultado"));
				}
			}
			result_reserva.close();
			cstmt.close();
		}
		catch (SQLException ex){
			logger.debug("Error al consultar la BD. SQLException: {}. SQLState: {}. VendorError: {}.", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		  	throw ex;
		}
		return numero_reserva;
		
		/*
		
		return numero_reserva;
		/*
		 * Datos estaticos de prueba: Quitar y reemplazar por código que invoca al S.P.
		 * 
		 * - Si pasajero tiene nro_doc igual a 1 retorna 101 codigo de reserva y si se pregunta por dicha reserva como dato de prueba resultado "Reserva confirmada"
		 * - Si pasajero tiene nro_doc igual a 2 retorna 102 codigo de reserva y si se pregunta por dicha reserva como dato de prueba resultado "Reserva en espera"
		 * - Si pasajero tiene nro_doc igual a 3 se genera una excepción, resultado "No hay asientos disponibles"
		 * - Si pasajero tiene nro_doc igual a 4 se genera una excepción, resultado "El empleado no es válido"
		 * - Si pasajero tiene nro_doc igual a 5 se genera una excepción, resultado "El pasajero no está registrado"
		 * - Si pasajero tiene nro_doc igual a 6 se genera una excepción, resultado "El vuelo no es válido"
		 * - Si pasajero tiene nro_doc igual a 7 se genera una excepción de conexión.
		 
		DAOReservaDatosPrueba.registrarReservaSoloIda(pasajero, vuelo, detalleVuelo, empleado);
		ReservaBean r = DAOReservaDatosPrueba.getReserva();
		logger.debug("Reserva: {}, {}", r.getNumero(), r.getEstado());
		int resultado = DAOReservaDatosPrueba.getReserva().getNumero();
		
		return resultado;
		// Fin datos estáticos de prueba.
		 * 
		 */
	}
	
	@Override
	public int reservarIdaVuelta(PasajeroBean pasajero, 
				 				 InstanciaVueloBean vueloIda,
				 				 DetalleVueloBean detalleVueloIda,
				 				 InstanciaVueloBean vueloVuelta,
				 				 DetalleVueloBean detalleVueloVuelta,
				 				 EmpleadoBean empleado) throws Exception {
		
		logger.info("Realiza la reserva de ida y vuelta con pasajero {}", pasajero.getNroDocumento());
		/**
		 * TODO (parte 2) Realizar una reserva de ida y vuelta llamando al Stored Procedure (S.P.) correspondiente. 
		 *      Si la reserva tuvo exito deberá retornar el número de reserva. Si la reserva no tuvo éxito o 
		 *      falla el S.P. deberá propagar un mensaje de error explicativo dentro de una excepción.
		 *      La demás excepciones generadas automáticamente por algun otro error simplemente se propagan.
		 *      
		 *      Nota: para acceder a la B.D. utilice la propiedad "conexion" que ya tiene una conexión
		 *      establecida con el servidor de B.D. (inicializada en el constructor DAOReservaImpl(...)).
		 * 
		 * @throws Exception. Deberá propagar la excepción si ocurre alguna. Puede capturarla para loguear los errores
		 *		   pero luego deberá propagarla para que se encargue el controlador.
		 *
		 * try (CallableStatement ... )
		 * {
		 *  ...
		 * }
		 * catch (SQLException ex){
		 * 			logger.debug("Error al consultar la BD. SQLException: {}. SQLState: {}. VendorError: {}.", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		 *  		throw ex;
		 * } 
		 */
		
		int numeroDeReserva = -1;
		 try {
			 
			 String sql = "CALL reservaIdaVuelta(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			 
			 CallableStatement cstmt = conexion.prepareCall(sql);  
			 
			 cstmt.setString(1, vueloIda.getNroVuelo());
			 cstmt.setDate(2, Fechas.convertirDateADateSQL(vueloIda.getFechaVuelo()));
			 cstmt.setString(3, detalleVueloIda.getClase());
			 cstmt.setString(4, vueloVuelta.getNroVuelo());
			 cstmt.setDate(5, Fechas.convertirDateADateSQL(vueloVuelta.getFechaVuelo()));
			 cstmt.setString(6, detalleVueloVuelta.getClase());
			 cstmt.setString(7, pasajero.getTipoDocumento());
			 cstmt.setInt(8, pasajero.getNroDocumento());
			 cstmt.setInt(9, empleado.getLegajo());	  

			 ResultSet rs = cstmt.executeQuery();
			 
			 if (rs.next()) {
				 String resultado = rs.getString("resultado");
				 logger.debug("Resultado reserva: {}.", rs.getString("resultado"));
	        	 if (resultado.equals("Reserva exitosa")) {
	        		 numeroDeReserva = rs.getInt("numero_reserva");
	        	 	 logger.debug(resultado + ". Numero de reserva: " + numeroDeReserva);
	        	 }
	        	 else {
		        	 logger.debug(resultado);
	        		 throw new Exception(resultado);
	        	 }
			 }	 
	         rs.close();
			 cstmt.close();
		  }
		  catch (SQLException ex){
		  		logger.debug("Error al consultar la BD. SQLException: {}. SQLState: {}. VendorError: {}.", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		   		throw ex;
		  } 
		return numeroDeReserva;
		
		/*
		 * Datos státicos de prueba: Quitar y reemplazar por código que invoca al S.P.
		 * 
		 * - Si pasajero tiene nro_doc igual a 1 retorna 101 codigo de reserva y si se pregunta por dicha reserva como dato de prueba resultado "Reserva confirmada"
		 * - Si pasajero tiene nro_doc igual a 2 retorna 102 codigo de reserva y si se pregunta por dicha reserva como dato de prueba resultado "Reserva en espera"
		 * - Si pasajero tiene nro_doc igual a 3 se genera una excepción, resultado "No hay asientos disponibles"
		 * - Si pasajero tiene nro_doc igual a 4 se genera una excepción, resultado "El empleado no es válido"
		 * - Si pasajero tiene nro_doc igual a 5 se genera una excepción, resultado "El pasajero no está registrado"
		 * - Si pasajero tiene nro_doc igual a 6 se genera una excepción, resultado "El vuelo no es válido"
		 * - Si pasajero tiene nro_doc igual a 7 se genera una excepción de conexión.
		 */		
		/*
		DAOReservaDatosPrueba.registrarReservaIdaVuelta(pasajero, vueloIda, detalleVueloIda, vueloVuelta, detalleVueloVuelta, empleado);
		int resultado = DAOReservaDatosPrueba.getReserva().getNumero();
		
		return resultado;
		// Fin datos estáticos de prueba.
		 */
	}
	
	@Override
	public ReservaBean recuperarReserva(int codigoReserva) throws Exception {
		
		logger.info("Solicita recuperar información de la reserva con codigo {}", codigoReserva);
		
		/**
		 * TODO (parte 2) Debe realizar una consulta que retorne un objeto ReservaBean donde tenga los datos de la
		 *      reserva que corresponda con el codigoReserva y en caso que no exista generar una excepción.
		 *
		 * 		Debe poblar la reserva con todas las instancias de vuelo asociadas a dicha reserva y 
		 * 		las clases correspondientes.
		 * 
		 * 		Los objetos ReservaBean además de las propiedades propias de una reserva tienen un arraylist
		 * 		con pares de instanciaVuelo y Clase. Si la reserva es solo de ida va a tener un unico
		 * 		elemento el arreglo, y si es de ida y vuelta tendrá dos elementos. 
		 * 
		 *      Nota: para acceder a la B.D. utilice la propiedad "conexion" que ya tiene una conexión
		 *      establecida con el servidor de B.D. (inicializada en el constructor DAOReservaImpl(...)).
		 */
		/*
		 * Importante, tenga en cuenta de setear correctamente el atributo IdaVuelta con el método setEsIdaVuelta en la ReservaBean
		 */
		// Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reales.
		ReservaBean reserva = null;
		boolean ida_vuelta = false;
		PasajeroBean pasajero = null;
		EmpleadoBean empleado = null;
		
		/** Objetos de InstanciaVueloClaseBean */
		
		String sql = "SELECT * FROM reservas natural join reserva_vuelo_clase WHERE numero = "+codigoReserva;
		logger.debug("SQL: {}", sql);
		try {
			Statement select = conexion.createStatement();
			ResultSet rs= select.executeQuery(sql);
			
			ida_vuelta = this.esIdayVuelta(codigoReserva);
			logger.debug("SQL: Llame a ida y vuelta {}",ida_vuelta);
			pasajero = this.obtenerPasajero(codigoReserva);
			logger.debug("SQL: Llame a obtener pasajero");
			empleado = this.obtenerEmpleado(codigoReserva);
			logger.debug("SQL: Llame a obtener empleado");
			

			while(rs.next()) {
				logger.debug("Se recupero la reserva con numero {} ", rs.getInt("numero"));
				if(rs.getInt("numero") == codigoReserva) {
					reserva = new ReservaBeanImpl();
					ArrayList<InstanciaVueloClaseBean> vuelosClase = this.obtenerAeropuertos(codigoReserva);
					
					/** Seteo los atributos de reserva */
					
					reserva.setEmpleado(empleado);
					reserva.setPasajero(pasajero);
					reserva.setEsIdaVuelta(ida_vuelta);
					
					reserva.setEstado(rs.getString("estado"));
					reserva.setFecha(rs.getDate("fecha"));
					reserva.setNumero(codigoReserva);
					reserva.setVencimiento(rs.getDate("vencimiento"));
					reserva.setVuelosClase(vuelosClase);
				}
			}
			select.close();
			rs.close();
		}catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		return reserva;
		/*ReservaBean reserva = DAOReservaDatosPrueba.getReserva();
		logger.debug("Se recuperó la reserva: {}, {}", reserva.getNumero(), reserva.getEstado());
		
		return reserva;*/
		// Fin datos estáticos de prueba.
	}
	private ArrayList<InstanciaVueloClaseBean> obtenerAeropuertos(int codigoReserva) throws Exception{
			ArrayList<InstanciaVueloClaseBean> toReturn=new ArrayList<InstanciaVueloClaseBean>();
			String sql = "SELECT * FROM reservas natural join reserva_vuelo_clase WHERE numero = "+codigoReserva;
			logger.debug("SQL: {}", sql);
			String codigo_vuelo="";
			Date fecha_vuelo=null;
			String clase="";
			UbicacionesBean ubicacion_sale = null;
			UbicacionesBean ubicacion_llega = null;
			try {
				Statement select = conexion.createStatement();
				ResultSet rs= select.executeQuery(sql);
				while(rs.next()) {
					codigo_vuelo = rs.getString("vuelo");
					fecha_vuelo = rs.getDate("fecha_vuelo");
					clase = rs.getString("clase");
					String sql_vuelos_disponibles = "SELECT * FROM vuelos_disponibles WHERE Nro_vuelo='"+codigo_vuelo+"' AND Fecha='"+fecha_vuelo+"' AND clase='"+clase+"'";
					logger.debug("SQL se recupera datos de la vista vuelos_disponibles {}", sql_vuelos_disponibles);
					Statement select_vuelos_disponibles = conexion.createStatement();
					ResultSet rs_vuelos_disponibles = select_vuelos_disponibles.executeQuery(sql_vuelos_disponibles);
						if(rs_vuelos_disponibles.next()) {
							/* ubicaciones */
							ubicacion_sale = new UbicacionesBeanImpl();
							ubicacion_sale.setCiudad(rs_vuelos_disponibles.getNString("ciudad_sale"));
							ubicacion_sale.setEstado(rs_vuelos_disponibles.getNString("estado_sale"));
							ubicacion_sale.setPais(rs_vuelos_disponibles.getNString("pais_sale"));
							String sql_huso_sale = "SELECT huso FROM ubicaciones WHERE pais='"+rs_vuelos_disponibles.getString("pais_llega")+"' AND estado='"+rs_vuelos_disponibles.getString("estado_llega")+"' AND ciudad='"+rs_vuelos_disponibles.getString("ciudad_llega")+"'";
							logger.debug("SQL: se recupera huso horario con: {}", sql_huso_sale);
							Statement select_huso_sale = conexion.createStatement();
							ResultSet rs_huso_sale = select_huso_sale.executeQuery(sql_huso_sale);
							if(rs_huso_sale.next()) {
								ubicacion_sale.setHuso(rs_huso_sale.getInt("huso"));
							}
							select_huso_sale.close();
							rs_huso_sale.close();
							ubicacion_llega = new UbicacionesBeanImpl();
							ubicacion_llega.setCiudad(rs_vuelos_disponibles.getNString("ciudad_llega"));
							ubicacion_llega.setEstado(rs_vuelos_disponibles.getNString("estado_llega"));
							ubicacion_llega.setPais(rs_vuelos_disponibles.getNString("pais_llega"));
							String sql_huso_llega = "SELECT huso FROM ubicaciones WHERE pais='"+rs_vuelos_disponibles.getString("pais_llega")+"' AND estado='"+rs_vuelos_disponibles.getString("estado_llega")+"' AND ciudad='"+rs_vuelos_disponibles.getString("ciudad_llega")+"'";
							logger.debug("SQL: se recupera huso horario con: {}", sql_huso_llega);
							Statement select_huso_llega = conexion.createStatement();
							ResultSet rs_huso_llega = select_huso_llega.executeQuery(sql_huso_llega);
							if(rs_huso_llega.next()) {
								ubicacion_llega.setHuso(rs_huso_llega.getInt("huso"));
							}
							select_huso_llega.close();
							rs_huso_llega.close();
							
							ArrayList<InstanciaVueloBean> aux=new ArrayList<InstanciaVueloBean>();
							DAOVuelosImpl obj = new DAOVuelosImpl(this.conexion);
							aux= obj.recuperarVuelosDisponibles(fecha_vuelo, ubicacion_sale, ubicacion_llega);
							logger.debug("Cantidad de aux: {}",aux.size());
							ArrayList<DetalleVueloBean> detalle =new ArrayList<DetalleVueloBean>();
			
							for(InstanciaVueloBean ivb : aux) {
								logger.debug("Codigo de vuelo es: {} - Ivb nro vuelo es: {}",codigo_vuelo,ivb.getNroVuelo());
								if (ivb.getNroVuelo().equals(codigo_vuelo)) {
									logger.debug("Codigo de vuelo es: {}",codigo_vuelo);
									InstanciaVueloClaseBean aux1 = new InstanciaVueloClaseBeanImpl();
									aux1.setVuelo(ivb);
									detalle = obj.recuperarDetalleVuelo(ivb);
									for (DetalleVueloBean det : detalle) {
										if (det.getClase().equals(clase)) {
											logger.debug("Clase es: {}",clase);
											aux1.setClase(det);
											toReturn.add(aux1);
										}
									}
								}
							}
							logger.debug("La cantidad de elementos de detalle es: {}",detalle.size());
						}
						select_vuelos_disponibles.close();
						rs_vuelos_disponibles.close();
				}
				rs.close();
				select.close();
			}catch (SQLException ex) {
				logger.error("SQLException: " + ex.getMessage());
				logger.error("SQLState: " + ex.getSQLState());
				logger.error("VendorError: " + ex.getErrorCode());
				throw new Exception("Error inesperado al consultar la B.D.");
		}
		return toReturn;
	}
		
	private EmpleadoBean obtenerEmpleado(int codigoReserva) throws Exception{
		String sql = "SELECT * FROM reservas natural join reserva_vuelo_clase WHERE numero = "+codigoReserva;
		logger.debug("SQL: {}", sql);
		EmpleadoBean empleado=null;
		try {
			Statement select = conexion.createStatement();
			ResultSet rs= select.executeQuery(sql);
			if(rs.next()) {
				int legajo = rs.getInt("legajo");
				DAOEmpleado dao_empleado = new DAOEmpleadoImpl(this.conexion);
				empleado = dao_empleado.recuperarEmpleado(legajo);
			}
			select.close();
			rs.close();
		}catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		return empleado;
	}
	private PasajeroBean obtenerPasajero(int codigoReserva) throws Exception{
		String sql = "SELECT * FROM reservas natural join reserva_vuelo_clase WHERE numero = "+codigoReserva;
		logger.debug("SQL: {}", sql);
		PasajeroBean pasajero=null;
		try {
			Statement select = conexion.createStatement();
			ResultSet rs= select.executeQuery(sql);
			if(rs.next()) {
				String doc_tipo = rs.getString("doc_tipo");
				int doc_nro = rs.getInt("doc_nro");
				DAOPasajero dao_pasajero = new DAOPasajeroImpl(this.conexion);
				pasajero = dao_pasajero.recuperarPasajero(doc_tipo, doc_nro);
			}
			select.close();
			rs.close();
		}catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		return pasajero;
	}
	private boolean esIdayVuelta(int codigoReserva) throws Exception{
		boolean toReturn = false;
		String sql = "SELECT COUNT(numero) AS reserva_realizada FROM reserva_vuelo_clase WHERE numero="+codigoReserva;
		logger.debug("SQL: {}", sql);
		try {
			Statement select = conexion.createStatement();
			ResultSet rs= select.executeQuery(sql);
			if(rs.next()) {
				if(rs.getInt("reserva_realizada") > 1)
					toReturn = true;
			}
			select.close();
			rs.close();
		}catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		return toReturn;
	}
	

}