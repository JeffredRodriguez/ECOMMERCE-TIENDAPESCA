package com.tiendapesca.APItiendapesca.Security;

import io.github.cdimascio.dotenv.Dotenv;

public class appconfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getDbUrl() {
        return dotenv.get("DB_URL");
    }

    public static String getDbUsername() {
        return dotenv.get("DB_USERNAME");
    }

    public static String getDbPassword() {
        return dotenv.get("DB_PASSWORD");
    }
    
    
    public static String getTokenPassword() {
        return dotenv.get("SECRET_KEYY");
    }
}
