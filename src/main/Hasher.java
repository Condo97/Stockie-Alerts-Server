package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Hasher {
    public static byte[] hash(String input) throws FileNotFoundException {
        File file = new File("salt.txt");
        Scanner sc = new Scanner(file);

        String salt = sc.nextLine();
        String saltedInput = input + salt;
        byte[] saltedInputBytes = saltedInput.getBytes();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(saltedInputBytes);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean verifyHash(String input, byte[] hash) throws FileNotFoundException {
        return hash(input) == hash;
    }
}
