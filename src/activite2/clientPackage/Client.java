package activite2.clientPackage;

import activite2.model.Operation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TP 2 - Activité 2-2 : Échange des Objets via Sockets TCP
 * 
 * Classe Client :
 * - Se connecte au serveur sur le port 1234
 * - Utilise ObjectOutputStream et ObjectInputStream pour échanger des objets sérialisés
 * - Permet la saisie de l'opération
 * - Crée une instance de la classe Operation (implements Serializable)
 * - Envoie l'objet au serveur
 * - Reçoit l'objet calculé et affiche le résultat
 */
public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 1234;

    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
    );

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CLIENT CALCULATRICE TCP (Activité 2-2 : Objets)  ");
        System.out.println("==================================================");

        try {
            System.out.println("[Client] Connexion au serveur " + SERVER_HOST + ":" + SERVER_PORT + "...");
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("[Client] Connecté avec succès !");

            // Initialisation des flux d'objets :
            // Règle d'or : créer ObjectOutputStream en premier et vider le buffer (flush)
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);

            System.out.println("\n--------------------------------------------------");
            System.out.println("Opérations supportées : +, -, *, /");
            System.out.println("Exemple d'opération : 55 * 25 ou 100 / 4");
            System.out.println("Pour quitter : tapez 'quitter', 'exit' ou '0'");
            System.out.println("--------------------------------------------------");

            while (true) {
                System.out.print("\nEntrez une opération (ou 'quitter') : ");
                String ligne = scanner.nextLine().trim();

                if (ligne.isEmpty()) {
                    continue;
                }

                if (ligne.equalsIgnoreCase("quitter") || ligne.equalsIgnoreCase("exit") || ligne.equals("0")) {
                    System.out.println("[Client] Demande de déconnexion...");
                    // Envoi de null pour signaler la fin au serveur
                    oos.writeObject(null);
                    oos.flush();
                    break;
                }

                // Validation syntaxique locale
                Matcher matcher = OPERATION_PATTERN.matcher(ligne);
                if (!matcher.matches()) {
                    System.err.println("[Client - Erreur] Syntaxe invalide ! Format attendu : <opérande1> <opérateur> <opérande2>");
                    System.err.println("  Exemple valide : 55 * 25 ou 10 + 20");
                    continue;
                }

                // Extraction des composants
                double op1 = Double.parseDouble(matcher.group(1));
                char operateur = matcher.group(2).charAt(0);
                double op2 = Double.parseDouble(matcher.group(3));

                // Étape b : Le client envoie un objet qui contient les arguments nécessaires à l'opération
                Operation operation = new Operation(op1, operateur, op2);
                System.out.println("[Client] Envoi de l'objet Operation : [" + op1 + " " + operateur + " " + op2 + "]");

                oos.writeObject(operation);
                oos.flush();
                oos.reset();

                // Étape g : Le client lit et affiche le résultat
                try {
                    Object reponseObj = ois.readObject();
                    if (reponseObj instanceof Operation) {
                        Operation reponse = (Operation) reponseObj;
                        if (reponse.isErreur()) {
                            System.err.println("[Client] Erreur reçue du serveur : " + reponse.getMessageErreur());
                        } else {
                            System.out.println("[Client] Résultat reçu : " + reponse.toString());
                        }
                    } else {
                        System.err.println("[Client] Type de réponse inattendu : " + reponseObj);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("[Client] Impossible de désérialiser la réponse : " + e.getMessage());
                }
            }

            // Fermeture des ressources
            ois.close();
            oos.close();
            socket.close();
            scanner.close();
            System.out.println("[Client] Déconnecté. Fin du programme.");

        } catch (IOException e) {
            System.err.println("[Client] Erreur de communication : " + e.getMessage());
        }
    }
}
