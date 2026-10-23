package activite2.model;

import java.io.Serializable;

/**
 * TP 2 - Activité 2-2 : Échange des Objets via Sockets TCP
 * 
 * Classe Operation :
 * - Implémente l'interface java.io.Serializable pour permettre sa transmission via les flux d'objets (ObjectInputStream / ObjectOutputStream)
 * - Contient les opérandes (op1, op2), l'opérateur ('+', '-', '*', '/'),
 *   le résultat du calcul, et les informations d'erreurs éventuelles.
 */
public class Operation implements Serializable {

    private static final long serialVersionUID = 1L;

    private double op1;
    private double op2;
    private char operateur;
    private double resultat;
    private boolean isErreur;
    private String messageErreur;

    /**
     * Constructeur utilisé par le client pour initialiser les arguments nécessaires au calcul.
     * 
     * @param op1 Premier opérande
     * @param operateur Opérateur (+, -, *, /)
     * @param op2 Deuxième opérande
     */
    public Operation(double op1, char operateur, double op2) {
        this.op1 = op1;
        this.operateur = operateur;
        this.op2 = op2;
        this.isErreur = false;
        this.messageErreur = null;
    }

    // Getters et Setters
    public double getOp1() {
        return op1;
    }

    public void setOp1(double op1) {
        this.op1 = op1;
    }

    public double getOp2() {
        return op2;
    }

    public void setOp2(double op2) {
        this.op2 = op2;
    }

    public char getOperateur() {
        return operateur;
    }

    public void setOperateur(char operateur) {
        this.operateur = operateur;
    }

    public double getResultat() {
        return resultat;
    }

    public void setResultat(double resultat) {
        this.resultat = resultat;
        this.isErreur = false;
    }

    public boolean isErreur() {
        return isErreur;
    }

    public void setErreur(boolean isErreur) {
        this.isErreur = isErreur;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
        this.isErreur = true;
    }

    @Override
    public String toString() {
        if (isErreur) {
            return "Operation [" + op1 + " " + operateur + " " + op2 + "] -> " + messageErreur;
        }
        // Formatage lisible du résultat (entier si entier, sinon décimal)
        String resStr = (resultat == (long) resultat) ? String.format("%d", (long) resultat) : String.valueOf(resultat);
        String op1Str = (op1 == (long) op1) ? String.format("%d", (long) op1) : String.valueOf(op1);
        String op2Str = (op2 == (long) op2) ? String.format("%d", (long) op2) : String.valueOf(op2);
        return op1Str + " " + operateur + " " + op2Str + " = " + resStr;
    }
}
