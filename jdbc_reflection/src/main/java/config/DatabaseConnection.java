package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://mysql-db:3306/plataforma";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static Connection instance;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Carrega o driver do MySQL
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Erro ao carregar o driver JDBC", e);
        }
    }

    public static Connection getConnection() throws SQLException {
    	if(instance == null) {
    		return DriverManager.getConnection(URL, USER, PASSWORD);    		
    	}
    	
    	return instance;
    }
}