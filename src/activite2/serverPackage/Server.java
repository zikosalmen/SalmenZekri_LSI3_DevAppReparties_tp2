package activite2.serverPackage;

import activite2.model.Operation;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * TP 2 - Activité 2-2 : Échange des Objets via Sockets TCP
 * 
 * Classe Serveur :
 * - Écoute sur le port 1234
 * - Utilise ObjectInputStream et ObjectOutputStream pour manipuler des objets sérialisés
 * - Reçoit l'objet Operation envoyé par le client (contenant op1, operateur, op2)
 * - Récupère les données de l'objet reçu (getters)
 * - Effectue le calcul demandé (+, -, *, /)
 * - Met à jour l'objet avec le résultat (ou l'erreur) et le renvoie au client
 */
public class Server {

    private static final int PORT = 1234;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SERVEUR CALCULATRICE TCP (Activité 2-2 : Objets) ");
        System.out.println("==================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Serveur] En attente de connexion d'un client sur le port " + PORT + "...");

            // Attente de connexion du client
            Socket socket = serverSocket.accept();
            System.out.println("[Serveur] Client connecté depuis : "
                    + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            // Initialisation des flux d'objets :
            // Règle d'or : créer ObjectOutputStream en premier et vider le buffer (flush),
            // pour envoyer le header de sérialisation et éviter un interblocage (deadlock).
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            while (true) {
                try {
                    // Étape c : Le serveur reçoit cet objet
                    Object obj = ois.readObject();

                    // Vérification de signal d'arrêt (si null ou déconnexion)
                    if (obj == null) {
                        System.out.println("[Serveur] Signal de déconnexion reçu.");
                        break;
                    }

                    if (obj instanceof Operation) {
                        Operation op = (Operation) obj;

                        // Étape d : Le serveur récupère les données de l'objet reçu
                        double op1 = op.getOp1();
                        char operateur = op.getOperateur();
                        double op2 = op.getOp2();

                        System.out.println("\n[Serveur] Objet reçu : " + op1 + " " + operateur + " " + op2);

                        // Étape e : Le serveur effectue le calcul demandé
                        switch (operateur) {
                            case '+':
                                op.setResultat(op1 + op2);
                                break;
                            case '-':
                                op.setResultat(op1 - op2);
                                break;
                            case '*':
                                op.setResultat(op1 * op2);
                                break;
                            case '/':
                                if (op2 == 0) {
                                    op.setMessageErreur("Erreur : Division par zéro impossible !");
                                } else {
                                    op.setResultat(op1 / op2);
                                }
                                break;
                            default:
                                op.setMessageErreur("Erreur : Opérateur '" + operateur + "' non supporté !");
                                break;
                        }

                        if (op.isErreur()) {
                            System.out.println("[Serveur] Traitement terminé avec notification : " + op.getMessageErreur());
                        } else {
                            System.out.println("[Serveur] Résultat calculé : " + op.getResultat());
                        }

                        // Étape f : Le serveur renvoie le résultat au client (sous forme de l'objet complété)
                        oos.writeObject(op);
                        oos.flush();
                        oos.reset(); // Crucial pour éviter la réutilisation en cache des objets par le flux
                        System.out.println("[Serveur] Objet renvoyé au client avec succès.");
                    } else {
                        System.err.println("[Serveur] Objet de type inattendu reçu : " + obj.getClass().getName());
                    }

                } catch (EOFException | SocketException e) {
                    System.out.println("\n[Serveur] Le client s'est déconnecté.");
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("[Serveur] Classe d'objet inconnue : " + e.getMessage());
                    break;
                }
            }

            // Fermeture des ressources
            ois.close();
            oos.close();
            socket.close();
            System.out.println("[Serveur] Connexion terminée. Serveur arrêté.");

        } catch (IOException e) {
            System.err.println("[Serveur] Erreur socket : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
