package activite1.clientPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * TP 2 - Activité 2-1 : Échange de Chaînes de Caractères via Sockets TCP
 * 
 * Classe Client :
 * - Se connecte au serveur sur le port 1234
 * - Utilise BufferedReader et PrintWriter pour l'échange de chaînes de caractères
 * - Valide la syntaxe de l'opération AVANT l'envoi (empêche l'envoi de chaînes
 *   incorrectes comme "25 ** 55" ou "abc + 3")
 * - Envoie l'opération entière ("55 * 25") ou les éléments de l'opération
 * - Reçoit et affiche le résultat retourné par le serveur
 */
public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 1234;

    // Expression régulière stricte pour valider la syntaxe de l'opération :
    // Exemple accepté : "55 * 25", "3.14 + 2", "-10 / 2", "100 - 45"
    // Exemple rejeté : "25 ** 55", "abc + 3", "12 ++ 4", "55 / "
    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
    );

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CLIENT CALCULATRICE TCP (Activité 2-1 : CC)    ");
        System.out.println("==================================================");

        try {
            System.out.println("[Client] Tentative de connexion au serveur " + SERVER_HOST + ":" + SERVER_PORT + "...");
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("[Client] Connecté avec succès au serveur !");

            // Initialisation des flux
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            System.out.println("\n--------------------------------------------------");
            System.out.println("Opérations supportées : + (addition), - (soustraction),");
            System.out.println("                        * (multiplication), / (division)");
            System.out.println("Formats acceptés : '55 * 25' ou 'menu' (saisie pas à pas)");
            System.out.println("Pour quitter     : tapez 'quitter', 'exit' ou '0'");
            System.out.println("--------------------------------------------------");

            while (true) {
                System.out.print("\nEntrez votre opération : ");
                String saisie = scanner.nextLine().trim();

                if (saisie.isEmpty()) {
                    continue;
                }

                // Vérification de la commande de sortie
                if (saisie.equalsIgnoreCase("quitter") || saisie.equalsIgnoreCase("exit") || saisie.equals("0")) {
                    out.println("quitter");
                    String reponseFin = in.readLine();
                    System.out.println("[Serveur] " + reponseFin);
                    System.out.println("[Client] Déconnexion en cours...");
                    break;
                }

                // Option saisie pas à pas
                if (saisie.equalsIgnoreCase("menu")) {
                    saisie = saisirPasAPas(scanner);
                    if (saisie == null) {
                        continue;
                    }
                }

                // VALIDATION DE LA SYNTAXE AVANT ENVOI (exigence énoncé)
                if (!validerSyntaxe(saisie)) {
                    System.err.println("[Client - Validation] ERREUR : Syntaxe de l'opération incorrecte !");
                    System.err.println("  -> La chaîne \"" + saisie + "\" a été bloquée localement.");
                    System.err.println("  -> Veuillez respecter le format : <nombre> <opérateur> <nombre> (ex: 55 * 25).");
                    continue;
                }

                // Si la syntaxe est valide, envoi au serveur
                System.out.println("[Client] Envoi de l'opération validée au serveur : \"" + saisie + "\"");
                out.println(saisie);

                // Lecture du résultat renvoyé par le serveur
                String reponse = in.readLine();
                if (reponse == null) {
                    System.out.println("[Client] Le serveur a interrompu la connexion.");
                    break;
                }

                System.out.println("[Client] Résultat reçu du serveur : " + reponse);
            }

            // Fermeture des ressources
            socket.close();
            scanner.close();
            System.out.println("[Client] Connexion fermée. Fin du programme.");

        } catch (IOException e) {
            System.err.println("[Client] Erreur de communication ou serveur indisponible : " + e.getMessage());
        }
    }

    /**
     * Valide la syntaxe de l'expression mathématique avec une expression régulière.
     * Empêche l'envoi de commandes incorrectes au serveur comme "25 ** 55" ou "abc + 3".
     */
    public static boolean validerSyntaxe(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        return OPERATION_PATTERN.matcher(expression.trim()).matches();
    }

    /**
     * Permet la saisie élément par élément : premier opérande, opérateur, deuxième opérande.
     */
    private static String saisirPasAPas(Scanner scanner) {
        System.out.println("--- Saisie guidée de l'opération ---");
        try {
            System.out.print("Entrez le premier opérande (ex: 55) : ");
            String op1Str = scanner.nextLine().trim();
            Double.parseDouble(op1Str); // validation locale

            System.out.print("Entrez l'opérateur (+, -, *, /) : ");
            String op = scanner.nextLine().trim();
            if (!op.matches("^[+\\-*/]$")) {
                System.err.println("[Client - Validation] Opérateur invalide ! Utilisez +, -, * ou /.");
                return null;
            }

            System.out.print("Entrez le deuxième opérande (ex: 25) : ");
            String op2Str = scanner.nextLine().trim();
            Double.parseDouble(op2Str); // validation locale

            return op1Str + " " + op + " " + op2Str;
        } catch (NumberFormatException e) {
            System.err.println("[Client - Validation] Opérande invalide ! Vous devez saisir un nombre réel ou entier.");
            return null;
        }
    }
}
