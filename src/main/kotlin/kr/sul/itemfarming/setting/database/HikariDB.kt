//package kr.sul.itemfarming.setting.database
//
//import com.zaxxer.hikari.HikariDataSource
//
//
//object HikariDB {
//    private val hikari = HikariDataSource()
//
//    // DB 설정
//    init {
//        val address = ""
//        val name = ""
//        val userName = ""
//        val password = ""
//
//        hikari.maximumPoolSize = 10
//        hikari.dataSourceClassName = "org.mariadb.jdbc.MariaDbDataSource"
//        hikari.addDataSourceProperty("serverName", address)
//        hikari.addDataSourceProperty("port", "3006")
//        hikari.addDataSourceProperty("databaseName", name)
//        hikari.addDataSourceProperty("user", userName)
//        hikari.addDataSourceProperty("password", password)
//    }
//}