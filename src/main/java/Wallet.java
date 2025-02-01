import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.math.BigInteger;

public class Wallet {
    /**
     * Generates a wallet address from a given private key.
     *
     * @param privateKeyHex The private key in hexadecimal format.
     * @return              The generated Ethereum wallet address.
     */
    public static String getWalletAddressFromPrivateKeyHex(String privateKeyHex){
        BigInteger privateKey = new BigInteger(privateKeyHex, CommonConstant.PRIVATE_KEY_RADIX);
        // Create an ECKeyPair object
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        // Generate a wallet address from the privateKey
        return CommonConstant.ADDRESS_PREFIX + Keys.getAddress(keyPair.getPublicKey());
    }

    public static boolean isValidAddress(String address){
        return WalletUtils.isValidAddress(address);
    }

    public static boolean isValidPrivateKey(String privateKey){
        return WalletUtils.isValidPrivateKey(privateKey);
    }

    public static void main(String[] args) {
        // Define the private key, change to your own private key
        String privateKeyHex = "4c0883a69102937d6231471b5dbb6204fe5129617082794b67c64c2289df5dc4";
        // Generate a wallet address from the privateKey
        String walletAddress = Wallet.getWalletAddressFromPrivateKeyHex(privateKeyHex);
        // Print the results
        System.out.println("The wallet address of [" + privateKeyHex + "] is: [" + walletAddress +"]");
        // if key and address are valid
        System.out.println("The wallet address [" + walletAddress + "] is " + (isValidAddress(walletAddress) ? "valid" : "not valid"));
        System.out.println("The private key [" + privateKeyHex + "] is " + (isValidPrivateKey(privateKeyHex) ? "valid" : "not valid"));
    }
}
