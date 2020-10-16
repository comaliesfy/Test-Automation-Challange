package ru.chellenge.task1.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.chellenge.task1.pojo.Man;


import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class Task01 {
    private static Connection connection;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    JSONArray manJsonArray = new JSONArray();

    @Before
    public void init() throws SQLException {
        //открыть подключение к бд
        connection = getConnection();
    }

    @After
    public void close() throws SQLException {
        //удаление таблицы
        deleteTable();
        //закрыть подключение
        connection.close();
    }

    @Test
    public void test() throws SQLException {
        //создание таблицы
        createTable();
        //доабвление строк
        Man man = new Man();
        man.setName("Лев");
        man.setSurname("Лещенко");
        man.setAge(78);
        man.setBirthdate("1942-02-01");
        insertManToTable(man.toString());

        man.setName("Леонид");
        man.setSurname("Агутин");
        man.setAge(52);
        man.setBirthdate("1968-07-16");
        insertManToTable(man.toString());

        //запрос на отображение всех данных таблицы
        ResultSet rs = findData();
        rs.next();
        //создание объекта данных первой строки
        Man man1 = (Man) setObject(rs);
        rs.next();
        //создание объекта данных второй строки
        Man man2 = (Man) setObject(rs);

        //проверка, что строки отличаются друг от друга по каждому полю
        assertNotEquals(man1.getName(), man2.getName());
        assertNotEquals(man1.getSurname(), man2.getSurname());
        assertNotEquals(man1.getAge(), man2.getAge());
        assertNotEquals(man1.getBirthdate(), man2.getBirthdate());

        //добавление объектов в массив
        manJsonArray.put(man1);
        manJsonArray.put(man2);
        //запись в файл
        createFile(gson.toJson(manJsonArray));
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://192.168.14.73:3306/challange?useUnicode=true&serverTimezone=UTC";
        String username = "root";
        String password = "root";
        Connection connection = DriverManager.getConnection(url, username, password);
        assertTrue(connection.isValid(1));
        return connection;
    }

    private void executeUpdate(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
    }

    private void createTable() throws SQLException {
        String table = "CREATE TABLE avorlova " +
                "(id int NOT NULL AUTO_INCREMENT," +
                "name varchar(40) NOT NULL," +
                "surname varchar(40)," +
                "age int," +
                "birthdate DATE," +
                "PRIMARY KEY (id)" +
                ")";
        executeUpdate(table);
    }

    private void insertManToTable(String man) throws SQLException {
        String insert = "INSERT INTO avorlova(name,surname,age,birthdate) " +
                "VALUES " + man + "";
        executeUpdate(insert);
    }

    private void deleteTable() throws SQLException {
        String table = "DROP TABLE avorlova";
        executeUpdate(table);
    }

    private ResultSet findData() throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery("SELECT * FROM avorlova");
    }

    private void createFile(String json) {
        try {
            FileWriter fileWriter = new FileWriter("avorlova.json");
            fileWriter.write(json);
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Man setObject(ResultSet rs) throws SQLException {
        Man man = new Man();
        man.setId(Integer.parseInt(rs.getString("id")));
        man.setName(rs.getString("name"));
        man.setSurname(rs.getString("surname"));
        man.setAge(Integer.parseInt(rs.getString("age")));
        man.setBirthdate(rs.getString("birthdate"));
        return man;
    }

}
