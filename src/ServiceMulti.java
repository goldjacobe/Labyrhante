/**
 * Classe Service tourne sur la machine Client
 * Assure l'écoute sur le port UDP du Client
 * Reçoie les messages transmis par le Serveur de la part des joueurs et les affiche sur la console
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ServiceMulti implements Runnable {

    final static int taille = 217;        // Taille des messages UDP  200 pour le msg + la taille du type de message et ses paramètres :                //  System.out.println("Message reçu : " + msg);				//Affichage de tout le message : MESP ...  Reste a n'affiche r QUE le message, pas la commande avec le message
    private MulticastSocket mso;
    byte[] message = new byte[taille];        // Tableau d'octet pour stocker le message reçu

    public ServiceMulti(int port, String ip) {
        try {
            this.mso = new MulticastSocket(port);
            mso.joinGroup(InetAddress.getByName(ip));
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }


    private void afficheMessageClient(String msg) {
        String id;
        String x;
        String y;
        String p;
        String mess = "";

        String[] messArray;
        String messClean = msg.replace("+++", ""); //Supprimer les +++ a la fin du msg reçu
        messArray = messClean.split(" ");

        switch (messArray[0]) {

            case "FANT":
                x = messArray[1];
                y = messArray[2];
                System.out.println("\t" + "Le fantôme est maintenant à la position x :" + x + " et y :" + y);
                break;

            case "SCOR":
                id = messArray[1];
                p = messArray[2];
                x = messArray[3];
                y = messArray[4];
                System.out.println("\t" + "Un fantôme fut attrapé par " + id + " à la pos x :" + x + " y :" + y + ". Il est maintenant à " + p + " points.");
                break;

            case "MESA":
                id = messArray[1];
                for (int i = 2; i < messArray.length; i++) {
                    mess += (messArray[i] + " ");
                }
                System.out.println("\t" + id + " envoie à tout le monde : " + mess);
                break;

            case "END":
                id = messArray[1];
                p = messArray[2];
                System.out.println("\t" + "La partie est terminé! " + id + " à gagner avec " + p + " points.");
                break;

        }
    }

    @Override
    public void run() {

        try {
            DatagramPacket paquet = new DatagramPacket(message, message.length);

            while (true) {
                mso.receive(paquet);
                String st = new String(paquet.getData(), 0, paquet.getLength());
                afficheMessageClient(st);
            }

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
