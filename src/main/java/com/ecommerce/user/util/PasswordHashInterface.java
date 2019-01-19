package com.ecommerce.user.util;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface PasswordHashInterface {
    public  String createHash(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException;
    public  boolean validatePassword(String password, String goodHash) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
