package vuelos.modelo.empleado.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vuelos.utils.Fechas;
import vuelos.modelo.empleado.beans.AeropuertoBean;
import vuelos.modelo.empleado.beans.AeropuertoBeanImpl;
import vuelos.modelo.empleado.beans.DetalleVueloBean;
import vuelos.modelo.empleado.beans.DetalleVueloBeanImpl;
import vuelos.modelo.empleado.beans.InstanciaVueloBean;
import vuelos.modelo.empleado.beans.InstanciaVueloBeanImpl;
import vuelos.modelo.empleado.beans.UbicacionesBean;
import vuelos.modelo.empleado.beans.UbicacionesBeanImpl;
import vuelos.modelo.empleado.dao.datosprueba.DAOVuelosDatosPrueba;

public class DAOVuelosImpl implements DAOVuelos {

	private static Logger logger = LoggerFactory.getLogger(DAOVuelosImpl.class);
	
	//conexión para acceder a la Base de Datos
	private Connection conexion;
	
	public DAOVuelosImpl(Connection conexion) {
		this.conexion = conexion;
	}

	@Override
	public ArrayList<InstanciaVueloBean> recuperarVuelosDisponibles(Date fechaVuelo, UbicacionesBean origen, UbicacionesBean destino)  throws Exception {
		/** 
		 * TODO Debe retornar una lista de vuelos disponibles para ese día con origen y destino según los parámetros. 
		 *      Debe propagar una excepción si hay algún error en la consulta.    
		 *      
		 *      Nota: para acceder a la B.D. utilice la propiedad "conexion" que ya tiene una conexión
		 *      establecida con el servidor de B.D. (inicializada en el constructor DAOVuelosImpl(...)).  
		 */
		//Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reales.
		String ciudadOrigen = origen.getCiudad();
		String ciudadDestino = destino.getCiudad();
		String sql = "SELECT DISTINCT nro_vuelo,modelo,dia_sale,hora_sale,hora_llega,tiempo_estimado,codigo_aero_sale,codigo_aero_llega,nombre_aero_sale,nombre_aero_llega,fecha,ciudad_sale FROM vuelos_disponibles WHERE"
				+ " ciudad_sale = " + "'" +ciudadOrigen+ "' AND "
				+ " ciudad_llega = " + "'" + ciudadDestino+ "' AND "
				+ " Fecha = '"+ Fechas.convertirDateAStringDB(fechaVuelo) +"'"; //importa vuelos.utils.Fechas
		logger.debug("SQL: {}", sql);
		ArrayList<InstanciaVueloBean> resultado = new ArrayList<InstanciaVueloBean>();
		try{ 
			 Statement select = conexion.createStatement();
			 ResultSet rs= select.executeQuery(sql);
			
			 while (rs.next()) {
				logger.debug("Se recuper� el vuelo con codigo {}", rs.getString("Nro_vuelo"));
				InstanciaVueloBean b= new InstanciaVueloBeanImpl(); 	
				AeropuertoBean aeroLlegada = new AeropuertoBeanImpl();
				aeroLlegada.setCodigo(rs.getString("codigo_aero_llega"));
				aeroLlegada.setNombre(rs.getString("nombre_aero_llega"));
				aeroLlegada.setUbicacion(origen);
				AeropuertoBean aeroSalida = new AeropuertoBeanImpl();
				aeroSalida.setCodigo(rs.getString("codigo_aero_sale"));
				aeroSalida.setNombre(rs.getString("nombre_aero_sale"));
				aeroSalida.setUbicacion(destino);
				
				b.setAeropuertoLlegada(aeroLlegada);
				b.setAeropuertoSalida(aeroSalida);
				b.setDiaSalida(rs.getString("dia_sale"));
				b.setFechaVuelo(rs.getDate("Fecha")); 
				b.setHoraLlegada(rs.getTime("hora_llega"));
				b.setHoraSalida(rs.getTime("hora_sale"));
				b.setModelo(rs.getString("Modelo"));
				b.setNroVuelo(rs.getString("Nro_vuelo"));
				b.setTiempoEstimado(rs.getTime("tiempo_estimado"));
				resultado.add(b);			
			  }			  
			return resultado;		
				
		}
		catch (SQLException ex)
		{			
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		/*
		ArrayList<InstanciaVueloBean> resultado = DAOVuelosDatosPrueba.generarVuelos(fechaVuelo);  
		
		return resultado;
		*/
		// Fin datos estáticos de prueba.
	}

	@Override
	public ArrayList<DetalleVueloBean> recuperarDetalleVuelo(InstanciaVueloBean vuelo) throws Exception {
		/** 
		 * TODO Debe retornar una lista de clases, precios y asientos disponibles de dicho vuelo.		   
		 *      Debe propagar una excepción si hay algún error en la consulta.    
		 *      
		 *      Nota: para acceder a la B.D. utilice la propiedad "conexion" que ya tiene una conexión
		 *      establecida con el servidor de B.D. (inicializada en el constructor DAOVuelosImpl(...)).
		 */
		//Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reales.
		String nro_vuelo = vuelo.getNroVuelo();
		String modelo = vuelo.getModelo();
		String dia_salida = vuelo.getDiaSalida();
		String hora_salida = vuelo.getHoraSalida().toString();
		String hora_llegada = vuelo.getHoraLlegada().toString();
		String tiempo_estimado = vuelo.getTiempoEstimado().toString();
		Date fecha = vuelo.getFechaVuelo();
		String nombre_aero_llega = vuelo.getAeropuertoLlegada().getNombre();
		String nombre_aero_sale = vuelo.getAeropuertoSalida().getNombre();
		
		ArrayList<DetalleVueloBean> resultado = new ArrayList<DetalleVueloBean>();
		String sql = "SELECT * from vuelos_disponibles WHERE"
				+ " Nro_vuelo = '" + nro_vuelo + "' AND "
				+ " Modelo = " + "'" + modelo + "' AND "
				+ " dia_sale = " + "'" + dia_salida + "' AND "
				+ " hora_sale= " + "'" + hora_salida + "' AND "
				+ " hora_llega = " + "'" + hora_llegada + "' AND "
				+ " tiempo_estimado = " + "'" + tiempo_estimado + "' AND "
				+ " nombre_aero_llega= " + "'" + nombre_aero_llega + "' AND "
				+ "nombre_aero_sale = " + "'" + nombre_aero_sale + "' AND "
				+ " Fecha = '"+ Fechas.convertirDateAStringDB(fecha) +"'";
		
		logger.debug("SQL: {}", sql);
		
		try{
			 Statement select = conexion.createStatement();
			 ResultSet rs= select.executeQuery(sql);
			 while(rs.next()) {
				 DetalleVueloBean b = new DetalleVueloBeanImpl();
				 b.setAsientosDisponibles(rs.getInt("asientos_disponibles"));
				 b.setClase(rs.getString("clase"));
				 b.setPrecio(rs.getFloat("Precio"));
				 b.setVuelo(vuelo);
				 resultado.add(b); 
			 }
			 
		}catch (SQLException ex)
		{			
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		return resultado;
		
		/*ArrayList<DetalleVueloBean> resultado = DAOVuelosDatosPrueba.generarDetalles(vuelo);
		
		return resultado;*/ 
		// Fin datos estáticos de prueba.
	}
}
