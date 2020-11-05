package ru.chellenge.task2;

import com.ibm.mq.jms.*;
import com.ibm.msg.client.wmq.compat.jms.internal.JMSC;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CONNECTION_MODE;

public class Task2 {

    private static QueueConnection queueConnection;
    private static MQQueueSession session;
    private static MQQueueSender senderIN;
    private static MQQueueReceiver receiverOUT;
    private static MQQueueReceiver receiverResp;
    CheckXMLValue cxv = new CheckXMLValue();

    private String host = "192.168.15.74";
    private int port = 4401;
    private String channel = "SYSTEM.DEF.SVRCONN";
    private String queueManagerName = "QMTESTERS1";
    private String queueIN = "queue:///TASK3.IN";
    private String queueOUT = "queue:///TASK3.OUT";
    private String queueRESP = "queue:///TASK3.RESP";

    @Before
    public void init() throws JMSException {
        queueConnection = createConnection();
        session = createSession();
        senderIN = createSenderIN();
        receiverOUT = createReceiverOUT();
        receiverResp = createReceiverResp();

    }

    @After
    public void close() throws JMSException {
        senderIN.close();
        receiverOUT.close();
        receiverResp.close();
        session.close();
        queueConnection.close();
    }

    @Test
    public void sendAllValidMessage() throws JMSException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        TextMessage message = createTextMessage("src/test/resources/ex3_msg1_valid.xml");
        //Проставление заголовков сообщения
        message.setStringProperty("System", "DMS");
        message.setStringProperty("TypeRequest", "loan");
        //Отправка сообщения в очередь IN
        senderIN.send(message);
        //проверка полученного значения из очереди Resp, сравнение с ожидаемым результататом
        assertReceiveMessageResp(receiveMessageResp(), "NULL", "[valid]");
        //проверка полученного значения из очереди OUT, сравнение с ожидаемым результататом Заголовка и проверка значений сообщения
        assertReceiveMessageOUT(receiveMessageOUT(), "OK");
    }


    @Test
    public void sendValidMessageWithoutHeaders() throws JMSException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        TextMessage message = createTextMessage("src/test/resources/ex3_msg1_valid.xml");
        //Отправка сообщения в очередь IN
        senderIN.send(message);
        assertReceiveMessageResp(receiveMessageResp(), "Invalid or Empty Header", "Parsing Error: Header values either invalid or empty");
    }

    @Test
    public void sendValidMessageWithoutHeadSystem() throws JMSException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        TextMessage message = createTextMessage("src/test/resources/ex3_msg1_valid.xml");
        //Проставление заголовков сообщения
        message.setStringProperty("System", "");
        message.setStringProperty("TypeRequest", "loan");
        //Отправка сообщения в очередь IN
        senderIN.send(message);

        assertReceiveMessageResp(receiveMessageResp(), "Invalid or Empty Header", "Parsing Error: Header values either invalid or empty");
    }

    @Test
    public void sendValidMessageWithoutHeadTypeRequest() throws JMSException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        TextMessage message = createTextMessage("src/test/resources/ex3_msg1_valid.xml");
        //Проставление заголовков сообщения
        message.setStringProperty("System", "DMS");
        message.setStringProperty("TypeRequest", "");
        //Отправка сообщения в очередь IN
        senderIN.send(message);
        assertReceiveMessageResp(receiveMessageResp(), "Invalid or Empty Header", "Parsing Error: Header values either invalid or empty");
    }

    @Test
    public void sendInvalidMessageWithHeaders() throws JMSException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        TextMessage message = createTextMessage("src/test/resources/ex3_msg3_invalid.xml");
        //Проставление заголовков сообщения
        message.setStringProperty("System", "DMS");
        message.setStringProperty("TypeRequest", "loan");
        //Отправка сообщения в очередь IN
        senderIN.send(message);
        assertReceiveMessageResp(receiveMessageResp(), "NULL", "The value 'WRONG' of element 'Currecny' is not valid");
    }

    public QueueConnection createConnection() throws JMSException {
        //Создание фабрики подключения и настройка
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        mqQueueConnectionFactory.setHostName(host);
        mqQueueConnectionFactory.setChannel(channel);
        mqQueueConnectionFactory.setPort(port);
        mqQueueConnectionFactory.setQueueManager(queueManagerName);
        mqQueueConnectionFactory.setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
        mqQueueConnectionFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
        //Создание подключения к менеджеру очередей
        QueueConnection queueConnection = mqQueueConnectionFactory.createQueueConnection();
        //Старт подключеня
        queueConnection.start();
        return queueConnection;
    }

    public MQQueueSession createSession() throws JMSException {
        //Создание сессии для работы с очередями с
        MQQueueSession session = (MQQueueSession) queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        return session;
    }

    public MQQueueSender createSenderIN() throws JMSException {
        //Создание очереди отправления
        MQQueue qIN = (MQQueue) session.createQueue(queueIN);
        MQQueueSender senderIN = (MQQueueSender) session.createSender(qIN);
        return senderIN;
    }

    public MQQueueReceiver createReceiverOUT() throws JMSException {
        //Создание очереди получения OUT
        MQQueue qOUT = (MQQueue) session.createQueue(queueOUT);
        MQQueueReceiver receiverOUT = (MQQueueReceiver) session.createReceiver(qOUT);
        return receiverOUT;
    }

    public MQQueueReceiver createReceiverResp() throws JMSException {
        //Создание очереди получения Resp
        MQQueue qResp = (MQQueue) session.createQueue(queueRESP);
        MQQueueReceiver receiverResp = (MQQueueReceiver) session.createReceiver(qResp);
        return receiverResp;
    }

    public TextMessage receiveMessageResp() throws JMSException, InterruptedException {
        //Полуение соощения из очереди REsp с ожиданием в 8 сек.
        Thread.sleep(8000);
        TextMessage receivedMessageResp = (TextMessage) receiverResp.receive(8000);
        return receivedMessageResp;
    }

    public void assertReceiveMessageResp(TextMessage receivedMessageResp, String headValue,String xmlValue) throws JMSException, IOException, SAXException, ParserConfigurationException {
        if (receivedMessageResp!=null) {
            //Проверка заголовка ERROR
            Assert.assertTrue(receivedMessageResp.getStringProperty("ERROR").contains(headValue));
            Document doc = convertMessageToXMLDoc(receivedMessageResp.getText());
            //Проверка значения тэга Validation-Result на соответствие ожидаемого знаения
            Assert.assertTrue(doc.getElementsByTagName("Validation-Result").item(0).getTextContent().contains(xmlValue));
        }
        else {
            System.out.println("queueRESP is empty");
        }
    }

    public TextMessage receiveMessageOUT() throws JMSException, InterruptedException {
        //Полуение соощения из очереди OUT с ожиданием в 8 сек.
        Thread.sleep(8000);
        TextMessage receivedMessageOUT = (TextMessage) receiverOUT.receive(8000);
        return receivedMessageOUT;
    }

    public void assertReceiveMessageOUT(TextMessage receivedMessageOUT, String validity) throws JMSException, IOException, SAXException, ParserConfigurationException {
        if (receivedMessageOUT!=null) {
            //Проверка заголовка сообщения
            Assert.assertEquals(validity, receivedMessageOUT.getStringProperty("ValidityCheck"));
            Document rm = convertMessageToXMLDoc(receivedMessageOUT.getText());
            //Сравнение полученных значений сообщения с ожидаемым результатом
            cxv.checkXmlValue(rm);
        }
        else {
            System.out.println("queueOUT is empty");
        }
    }

    private TextMessage createTextMessage(String xmlPath) throws IOException, JMSException {
        String xml = new String(Files.readAllBytes(Paths.get(xmlPath)));
        //Создание сообщения для отравки в очредь IN
        return session.createTextMessage(xml);
    }

    //Конвертор String из сообщения в формат Document
    static Document convertMessageToXMLDoc(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        return doc;
    }

}