package org.example.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for encrypting and decrypting long integer IDs using AES encryption.
 */
public class SharingCodeUtil {
    private static final String key = "shorekeeper_wife";

    /**
     * Encrypts a long integer ID using AES encryption.
     * @param id  The long integer ID to encrypt
     * @return The encrypted ID as a hexadecimal string
     * @throws Exception If an error occurs during encryption
     */
    public static String encrypt(Long id) {
        // 创建 AES 密钥
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

        // 创建 Cipher 对象
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

        // 初始化 Cipher 为加密模式
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        // 加密输入文本
        byte[] encryptedBytes;
        try {
            encryptedBytes = cipher.doFinal(id.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }

        // 将加密字节转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : encryptedBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * Decrypts a hexadecimal string using AES encryption.
     * @param hexInput The hexadecimal string to decrypt as a long integer ID
     * @return The decrypted long integer ID
     * @throws Exception If an error occurs during decryption
     */
    public static Long decrypt(String hexInput) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 将十六进制字符串转换为字节数组
        byte[] encryptedBytes = new byte[hexInput.length() / 2];
        for (int i = 0; i < hexInput.length(); i += 2) {
            encryptedBytes[i / 2] = (byte) ((Character.digit(hexInput.charAt(i), 16) << 4)
                    + Character.digit(hexInput.charAt(i + 1), 16));
        }

        // 解密输入字节
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return Long.parseLong(new String(decryptedBytes, StandardCharsets.UTF_8).trim()); // 去除空格
    }
}
