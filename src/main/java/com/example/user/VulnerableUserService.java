package com.example.user;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Random;

// Intentionally vulnerable demo code — mirrors sample-spring-boot-app's
// VulnerableWorkoutService.java. Exists solely to give SAST/security scanners
// (SonarQube now; Trivy/Grype later) findings to push into Unify Security Center.
// Do not use any of these patterns in real code.
@Service
public class VulnerableUserService {

    // S6437 VULNERABILITY (CRITICAL): Hardcoded API credentials
    private static final String AUTH0_CLIENT_SECRET = "fake_client_secret_1234567890abcdef_demo_only";
    private static final String DB_ADMIN_PASSWORD = "user_service_admin_2024!";

    // S3649 VULNERABILITY (HIGH): SQL injection via raw string concatenation
    public void findUserByUsername(String username) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        Statement stmt = conn.createStatement();
        // Noncompliant: user-controlled input directly concatenated into SQL query
        stmt.executeQuery("SELECT * FROM users WHERE username = '" + username + "'");
        conn.close();
    }

    // S2076 VULNERABILITY (HIGH): OS command injection
    public String runAccountDiagnostic(String host) throws Exception {
        // Noncompliant: user-controlled data injected into OS command
        Process proc = Runtime.getRuntime().exec("ping -c 1 " + host);
        return "exit=" + proc.exitValue();
    }

    // S3329 VULNERABILITY (MEDIUM): AES used with insecure ECB mode
    public byte[] encryptProfileData(String data) throws Exception {
        SecretKeySpec key = new SecretKeySpec("1234567890123456".getBytes(), "AES");
        // Noncompliant: ECB mode is deterministic and reveals patterns in plaintext
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes());
    }

    // S4790 VULNERABILITY (MEDIUM): MD5 used for password hashing
    public String hashUserPassword(String password) throws Exception {
        // Noncompliant: MD5 is cryptographically broken and unsuitable for passwords
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // S2083 VULNERABILITY (HIGH): Path traversal — user input used to construct file path
    public byte[] readUserAvatar(String filename) throws Exception {
        // Noncompliant: filename is user-controlled and not sanitized
        File file = new File("/var/data/avatars/" + filename);
        FileInputStream fis = new FileInputStream(file);
        return fis.readAllBytes();
    }

    // S5131 VULNERABILITY (HIGH): XSS — user input reflected into HTML response without escaping
    public String renderUserProfile(String displayName) {
        // Noncompliant: user-controlled value written directly into HTML
        return "<html><body><h1>Profile: " + displayName + "</h1></body></html>";
    }

    // S2245 VULNERABILITY (MEDIUM): Use of a predictable pseudo-random number generator
    // for a security-sensitive value (password reset token)
    public String generatePasswordResetToken() {
        // Noncompliant: java.util.Random is not cryptographically secure
        Random random = new Random();
        return String.valueOf(random.nextLong());
    }

    // S5042 VULNERABILITY (HIGH): Deserialization of untrusted data
    public Object deserializeUserSession(byte[] sessionData) throws Exception {
        // Noncompliant: deserializing attacker-controlled bytes can lead to RCE
        ObjectInputStream ois = new ObjectInputStream(new java.io.ByteArrayInputStream(sessionData));
        return ois.readObject();
    }
}
