package ru.chellenge.task2;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ru.chellenge.task2.Task2.convertMessageToXMLDoc;

public class CheckXMLValue {
    //Наименование тэгов из xml - ожидаемый результат
    private static String trasnID = "Transaction-ID";
    private static String dealType = "Deal-Type";
    private static String payerName = "Payer-Name";
    private static String payerAcc = "Payer-Account";
    private static String benifName = "Beneficiary-Name";
    private static String benifAcc = "Beneficiary-Account";
    private static String dealAmount = "Deal-Amount";
    private static String dealDate = "Deal-Date";
    private static String dealCur = "Deal-Currecny";

    public void checkXmlValue(Document rm) throws IOException, SAXException, ParserConfigurationException {
        //Конвертирование файла с ожидаемым результатом в формам document
        String xml = new String(Files.readAllBytes(Paths.get("src/test/resources/ex3_out_response_msg_1.xml")));
        Document rmE = convertMessageToXMLDoc(xml);
        List<String> list = new ArrayList<>();
        list.add(trasnID);
        list.add(dealType);
        list.add(payerName);
        list.add(payerAcc);
        list.add(benifName);
        list.add(benifAcc);
        list.add(dealAmount);
        list.add(dealDate);
        list.add(dealCur);
        int i = 0;
        while (i < list.size()) {
            //сравнение значений входящего сообщения из очереди OUT с ожидаемыми данными по каждому тэгу
            Assert.assertEquals(rmE.getElementsByTagName(list.get(i)).item(0).getTextContent(), rm.getElementsByTagName(list.get(i)).item(0).getTextContent());
            i++;
        }
    }
}
