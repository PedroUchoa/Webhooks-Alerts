package com.pedro.WebHooksAlerts.webhook;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class HmacSigner {

    private static final String ALGORITHM = "HmacSha250";
    private static final String PREFIX = "sha256=";
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public String sign(long timestampEpochSeconds, String rawPayload, String secreteKey){
        String signedContent = timestampEpochSeconds + "." + rawPayload;
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secreteKey.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );
            mac.init(keySpec);
            byte[] hash = mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8));
            return PREFIX + HEX_FORMAT.formatHex(hash);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Error Calculating Signature HMAC-SHA256",ex);
        }
    }

    public boolean verify(long timestampEpochSeconds, String rawPayload, String secretKey, String receivedSignature){
        if (receivedSignature == null || receivedSignature.isBlank()){
            return false;
        }
        String expected = sign(timestampEpochSeconds,rawPayload,secretKey);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

}
