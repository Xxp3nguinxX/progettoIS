package client;
//ciuccia
import java.util.Hashtable;
import java.util.Random;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.*;
import javax.naming.*;

public class Client {

    public static void main(String[] args){

        Hashtable<String,String> p = new Hashtable<String,String>();

        p.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        p.put("java.naming.provider.url","tcp://127.0.0.1:61616");

        p.put("queue.Richiesta","Richiesta");
        p.put("queue.Risposta", "Risposta");

        
        try {
            InitialContext ctx = new InitialContext(p);

            QueueConnectionFactory qconnf = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");
            Queue queueRequest = (Queue) ctx.lookup("Richiesta");
            Queue queueResponse = (Queue) ctx.lookup("Risposta");
    
            QueueConnection qconn = qconnf.createQueueConnection();
            qconn.start();

            //creo il receiver 

            QueueSession qsession = qconn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);


            QueueReceiver qReceiver = qsession.createReceiver(queueResponse);
            ClientListener l = new ClientListener();
            qReceiver.setMessageListener(l);

            // creo il sender

            QueueSender qsender = qsession.createSender(queueRequest);

            // Devo inviare i messaggi, prima devo crearli e ricordare di settare il JMSReplyTo

            MapMessage mm = qsession.createMapMessage();

            for(int i = 0;i<10;i++){

                if(i%2==0){

                    //Deposita
                    //creo il body del MM 

                    mm.setString("operazione", "deposita");

                    Random r = new Random();
                    int r_value = r.nextInt(100);
                    mm.setInt("valore", r_value);

                    qsender.send(mm);

                    System.out.println("[CLIENT] Inviato messaggio deposita" + r_value);

                }
                else{

                    //Preleva, deve anche specificare il JMSReplyTo
                    mm.setString("operazione", "preleva");
                    mm.setJMSReplyTo(queueResponse);

                    qsender.send(mm);

                    System.out.println("[CLIENT] Inviato messaggio preleva");

                }
            }


        } catch (NamingException e) {
            
            System.out.println("Errore NAMING");
        } catch (JMSException e) {
            //System.out.println("Errore JMS");
            e.printStackTrace();
        }

    }
    
}
