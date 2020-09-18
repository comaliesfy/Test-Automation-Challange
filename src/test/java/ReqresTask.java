import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ReqresTask {

    @Test
    public void getUsersCheckEmail() {
        Response response = RestAssured.given()
                .get("https://reqres.in/api/users?page=1");
        assertEquals(200, response.getStatusCode());
        JsonPath body = response.getBody().jsonPath();
        Assert.assertEquals(6, body.getList("data.id").size());
        Assert.assertTrue(body.getList("data.email").contains("emma.wong@reqres.in"));
        Assert.assertTrue(body.getList("data.email").contains("george.bluth@reqres.in"));
    }
    //Убедиться, что GET запрос на https://reqres.in/api/users?page=1 возвращает 6 пользователей, среди которых есть пользователи с email george.bluth@reqres.in и emma.wong@reqres.in

    @Test
    public void createNewUser() throws ParseException {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .post("https://reqres.in/api/users");
        assertEquals(201, response.getStatusCode());
        Date parseDate = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSSSS'Z'").parse(response.getBody().jsonPath().get("createdAt"));
        SimpleDateFormat myFormat = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println(myFormat.format(parseDate));
    }
    //Создать пользователя с помощью POST запроса https://reqres.in/api/users и вывести в консоль дату создания из ответа от сервиса в формате ДД.ММ.ГГГГ
}
