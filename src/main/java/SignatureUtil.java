import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

public class SignatureUtil {
    private static final int PRIVATE_KEY_RADIX = 16;
    private static final int SIGNATURE_BYTE_LENGTH = 65;
    private static final int V_INDEX = 64;
    private static final int V_BASE = 27;
    private static final int V_LOWER_BOUND = 27;
    private static final int R_START_INDEX = 0;
    private static final int R_END_INDEX = 32;
    private static final int S_START_INDEX = 32;
    private static final int S_END_INDEX = 64;
    private static final String ADDRESS_PREFIX = "0x";

    /**
     * Validates a given signature against the specified message and wallet address.
     *
     * @param signature     The digital signature to validate, in hexadecimal format.
     * @param message         The message (original message) that was signed.
     * @param walletAddress The wallet address expected to match the signature.
     * @return              True if the signature is valid and corresponds to the wallet
     *                      address; false otherwise.
     */
    public static Boolean isSignatureValid(String signature, String message, String walletAddress) {
        if (StringUtils.isAnyBlank(signature, message, walletAddress)) {
            return false;
        }

        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        if (signatureBytes.length != SIGNATURE_BYTE_LENGTH) {
            return false;
        }

        byte v = signatureBytes[V_INDEX];
        if (v < V_LOWER_BOUND) {
            v += V_BASE;
        }
        Sign.SignatureData signatureData = new Sign.SignatureData(
                v,
                Arrays.copyOfRange(signatureBytes, R_START_INDEX, R_END_INDEX),
                Arrays.copyOfRange(signatureBytes, S_START_INDEX, S_END_INDEX)
        );
        BigInteger publicKey;
        try {
            publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), signatureData);
        } catch (SignatureException e) {
            return false;
        }
        String parsedAddress = ADDRESS_PREFIX + Keys.getAddress(publicKey);
        return parsedAddress.equalsIgnoreCase(walletAddress);
    }

    /**
     * Signs a message using a given private key and returns the signature in hexadecimal format.
     * This function applies the Ethereum-specific prefix to the message before signing.
     *
     * @param privateKeyHex The private key in hexadecimal format.
     * @param message       The message to be signed.
     * @return              The generated signature as a hexadecimal string.
     */
    public static String signPrefixedMessage(String privateKeyHex, String message) {
        BigInteger privateKey = new BigInteger(privateKeyHex, PRIVATE_KEY_RADIX);

        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Sign.SignatureData signatureData = Sign.signPrefixedMessage(message.getBytes(), keyPair);

        return Numeric.toHexStringNoPrefix(signatureData.getR()) +
                Numeric.toHexStringNoPrefix(signatureData.getS()) +
                Numeric.toHexStringNoPrefix(signatureData.getV());
    }

    /**
     * Generates a wallet address from a given private key.
     *
     * @param privateKeyHex The private key in hexadecimal format.
     * @return              The generated Ethereum wallet address.
     */
    public static String getWalletAddressFromPrivateKeyHex(String privateKeyHex){
        BigInteger privateKey = new BigInteger(privateKeyHex, PRIVATE_KEY_RADIX);
        // Create an ECKeyPair object
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        // Generate a wallet address from the privateKey
        return ADDRESS_PREFIX + Keys.getAddress(keyPair.getPublicKey());
    }

    public static void main(String[] args) {
        // Define the private key, change to your own private key
        String privateKeyHex = "4c0883a69102937d6231471b5dbb6204fe5129617082794b67c64c2289df5dc4";

        // Define the message to be signed
        String message = "2778f9e5-5992-4b06-8a8f-85135d687cff";

        // Sign the message using the private key
        String signature = signPrefixedMessage(privateKeyHex, message);

        // Generate a wallet address from the privateKey
        String walletAddress = getWalletAddressFromPrivateKeyHex(privateKeyHex);

        // Validate the signature
        boolean isValid = isSignatureValid(signature, message, walletAddress);

        // Print the results
        System.out.println("Generated Wallet Address: " + walletAddress);
        System.out.println("Signature: " + signature);
        System.out.println("Is signature valid: " + isValid);
    }
}
