import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Wallet {

    /**
     * Generates a random Ethereum private key.
     *
     * @return A randomly generated private key in hexadecimal format.
     * @throws InvalidAlgorithmParameterException If the cryptographic algorithm parameters are invalid.
     * @throws NoSuchAlgorithmException If the cryptographic algorithm is not available.
     * @throws NoSuchProviderException If the security provider is not available.
     */
    public static String createRandomPrivateKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // Generate a random ECKeyPair (contains both private and public keys)
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        // Return the private key as a hexadecimal string
        return ecKeyPair.getPrivateKey().toString(CommonConstant.PRIVATE_KEY_RADIX);
    }

    /**
     * Generates an Ethereum wallet address from a given private key.
     *
     * @param privateKeyHex The private key in hexadecimal format.
     * @return The generated Ethereum wallet address (starting with "0x").
     */
    public static String getWalletAddressFromPrivateKeyHex(String privateKeyHex) {
        // Convert the private key from hexadecimal format to BigInteger
        BigInteger privateKey = new BigInteger(privateKeyHex, CommonConstant.PRIVATE_KEY_RADIX);
        // Create an ECKeyPair object (contains both private and public keys)
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        // Generate a wallet address from the public key and return it with "0x" prefix
        return CommonConstant.ADDRESS_PREFIX + Keys.getAddress(keyPair.getPublicKey());
    }

    /**
     * Validates whether a given Ethereum wallet address is in a correct format.
     *
     * @param address The Ethereum wallet address (should start with "0x").
     * @return True if the address is valid, false otherwise.
     */
    public static boolean isValidAddress(String address) {
        return WalletUtils.isValidAddress(address);
    }

    /**
     * Validates whether a given private key is valid.
     *
     * @param privateKey The private key in hexadecimal format.
     * @return True if the private key is valid, false otherwise.
     */
    public static boolean isValidPrivateKey(String privateKey) {
        return WalletUtils.isValidPrivateKey(privateKey);
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // Generate a random private key
        String privateKeyHex = Wallet.createRandomPrivateKey();

        // Generate a wallet address from the private key
        String walletAddress = Wallet.getWalletAddressFromPrivateKeyHex(privateKeyHex);

        // Print the generated private key and wallet address
        System.out.println("The wallet address of [" + privateKeyHex + "] is: [" + walletAddress + "]");

        // Validate if the generated wallet address is correct
        System.out.println("The wallet address [" + walletAddress + "] is " + (Wallet.isValidAddress(walletAddress) ? "valid" : "not valid"));

        // Validate if the generated private key is correct
        System.out.println("The private key [" + privateKeyHex + "] is " + (Wallet.isValidPrivateKey(privateKeyHex) ? "valid" : "not valid"));
    }
}
