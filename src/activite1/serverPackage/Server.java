package activite1.serverPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TP 2 - Activité 2-1 : Échange de Chaînes de Caractères via Sockets TCP
 * 
 * Classe Serveur :
 * - Écoute sur le port 1234
 * - Utilise BufferedReader et PrintWriter pour la gestion des flux de chaînes de caractères
 * - Reçoit l'opération envoyée par le client (ex: "55 * 25")
 * - Calcule le résultat (+, -, *, /)
 * - Gère les erreurs (division par zéro, opérateur inconnu)
 * - Renvoie le résultat au format chaîne de caractères au client
 */
public class Server {

    private static final int PORT = 1234;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SERVEUR CALCULATRICE TCP (Activité 2-1 : CC)   ");
        System.out.println("==================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Serveur] En attente de connexion d'un client sur le port " + PORT + "...");

            // Attente de connexion du client
            Socket socket = serverSocket.accept();
            System.out.println("[Serveur] Client connecté depuis : "
                    + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            // Initialisation des flux de traitement de chaînes de caractères
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String ligne;
            while ((ligne = in.readLine()) != null) {
                ligne = ligne.trim();

                // Vérification de la demande de déconnexion
                if (ligne.equalsIgnoreCase("quitter") || ligne.equalsIgnoreCase("exit") || ligne.equals("0")) {
                    System.out.println("[Serveur] Demande de déconnexion reçue du client. Fermeture de session...");
                    out.println("Déconnexion acceptée. Au revoir !");
                    break;
                }

                System.out.println("\n[Serveur] Opération reçue du client : \"" + ligne + "\"");

                // Traitement et calcul de l'opération
                String reponse = calculerOperation(ligne, in);

                System.out.println("[Serveur] Résultat calculé : " + reponse);

                // Envoi de la réponse au client
                out.println(reponse);
                System.out.println("[Serveur] Résultat envoyé avec succès au client.");
            }

            // Fermeture de la socket client
            socket.close();
            System.out.println("\n[Serveur] Connexion client fermée. Serveur arrêté.");

        } catch (IOException e) {
            System.err.println("[Serveur] Erreur de communication : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Analyse et calcule l'opération fournie.
     * Prend en charge soit une ligne complète (ex: "55 * 25" ou "55+25"),
     * soit une saisie élément par élément si nécessaire.
     */
    private static String calculerOperation(String ligne, BufferedReader in) {
        try {
            double op1;
            char operateur;
            double op2;

            // Détection si l'opération est passée en une seule chaîne (ex: "55 * 25")
            // Expression régulière séparant opérande 1, opérateur (+, -, *, /), et opérande 2
            String regex = "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$";
            if (ligne.matches(regex)) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                java.util.regex.Matcher matcher = pattern.matcher(ligne);
                if (matcher.find()) {
                    op1 = Double.parseDouble(matcher.group(1));
                    operateur = matcher.group(2).charAt(0);
                    op2 = Double.parseDouble(matcher.group(3));
                } else {
                    return "Erreur : Format d'opération non reconnu !";
                }
            } else {
                // Cas alternatif : l'opérande 1 est seule sur la première ligne,
                // lecture des lignes suivantes pour opérateur et opérande 2
                op1 = Double.parseDouble(ligne);
                String opStr = in.readLine();
                if (opStr == null) return "Erreur : Opérateur manquant !";
                operateur = opStr.trim().charAt(0);

                String op2Str = in.readLine();
                if (op2Str == null) return "Erreur : Deuxième opérande manquante !";
                op2 = Double.parseDouble(op2Str.trim());
            }

            // Exécution du calcul selon l'opérateur
            double resultat;
            switch (operateur) {
                case '+':
                    resultat = op1 + op2;
                    break;
                case '-':
                    resultat = op1 - op2;
                    break;
                case '*':
                    resultat = op1 * op2;
                    break;
                case '/':
                    if (op2 == 0) {
                        return "Erreur : Division par zéro impossible !";
                    }
                    resultat = op1 / op2;
                    break;
                default:
                    return "Erreur : Opérateur '" + operateur + "' non valide (utilisez +, -, *, /) !";
            }

            // Formatage du résultat : entier si pas de décimales, sinon décimal
            if (resultat == (long) resultat) {
                return String.format("%d", (long) resultat);
            } else {
                return String.valueOf(resultat);
            }

        } catch (NumberFormatException e) {
            return "Erreur : Nombres invalides dans l'expression !";
        } catch (IOException e) {
            return "Erreur lors de la lecture des données : " + e.getMessage();
        }
    }
}
