package vuelos.modelo.empleado.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vuelos.modelo.empleado.beans.EmpleadoBean;
import vuelos.modelo.empleado.beans.EmpleadoBeanImpl;

public class DAOEmpleadoImpl implements DAOEmpleado {

	private static Logger logger = LoggerFactory.getLogger(DAOEmpleadoImpl.class);
	
	//conexión para acceder a la Base de Datos
	private Connection conexion;
	
	public DAOEmpleadoImpl(Connection c) {
		this.conexion = c;
	}


	@Override
	public EmpleadoBean recuperarEmpleado(int legajo) throws Exception {
		logger.info("recupera el empleado que corresponde al legajo {}.", legajo);
		
		/**
		 * TODO Debe recuperar de la B.D. los datos del empleado que corresponda al legajo pasado 
		 *      como parámetro y devolver los datos en un objeto EmpleadoBean. Si no existe el legajo 
		 *      deberá retornar null y si ocurre algun error deberá generar y propagar una excepción.	
		 *       
		 *      Nota: para acceder a la B.D. utilice la propiedad "conexion" que ya tiene una conexión
		 *      establecida con el servidor de B.D. (inicializada en el constructor DAOEmpleadoImpl(...)). 
		 */		
		EmpleadoBean empleado = null;
		String sql = "SELECT * FROM empleados WHERE legajo = "+legajo;
		logger.debug("SQL: {}", sql);
		try {
			Statement select = conexion.createStatement();
			ResultSet rs= select.executeQuery(sql);
			while (rs.next()) {
				logger.debug("Se recupero el empleado con legajo {} ", rs.getInt("legajo"));
				if(rs.getInt("legajo") == legajo) {
					empleado = new EmpleadoBeanImpl();
					empleado.setLegajo(legajo);
					empleado.setPassword(rs.getString("password"));
					empleado.setNroDocumento(rs.getInt("doc_nro"));
					empleado.setTipoDocumento(rs.getString("doc_tipo"));
					empleado.setApellido(rs.getString("apellido"));
					empleado.setNombre(rs.getString("nombre"));
					empleado.setDireccion(rs.getString("direccion"));
					empleado.setTelefono(rs.getString("telefono"));
				}			
			  }	
		}catch (SQLException ex) {
			logger.error("SQLException: " + ex.getMessage());
			logger.error("SQLState: " + ex.getSQLState());
			logger.error("VendorError: " + ex.getErrorCode());
			throw new Exception("Error inesperado al consultar la B.D.");
		}
		return empleado;
		/*
		 * Datos estáticos de prueba. Quitar y reemplazar por código que recupera los datos reales.  
			
			
		
		EmpleadoBean empleado = null;
		
		empleado = new EmpleadoBeanImpl();
		empleado.setLegajo(9);
		empleado.setApellido("ApEmp9");
		empleado.setNombre("NomEmp9");
		empleado.setTipoDocumento("DNI");
		empleado.setNroDocumento(9);
		empleado.setDireccion("DirEmp9");
		empleado.setTelefono("999-9999");
		empleado.setCargo("Empleado de Prestamos");
		empleado.setPassword("45c48cce2e2d7fbdea1afc51c7c6ad26"); // md5(9);
		empleado.setNroSucursal(7);
		
		return empleado;
		*/
		// Fin datos estáticos de prueba.
	}

}
