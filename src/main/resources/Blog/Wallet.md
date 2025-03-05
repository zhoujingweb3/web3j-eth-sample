# Ethereum Wallet Operations in Java
🌍 **Languages:** [English](Wallet.md) | [简体中文](Wallet.zh.md)

Web3j is a very useful tool, but it can be a bit complex to use. Therefore, I have written some usage examples in the hope that they will be helpful to everyone.  
This chapter focuses on wallet-related operations.

## Dependencies
### Maven Dependency
```xml
<!-- web3j -->
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>5.0.0</version>
</dependency>
```
### Constants Class `CommonConstant.java`
```java
public class CommonConstant {
    public static final int PRIVATE_KEY_RADIX = 16;
    public static final int SIGNATURE_BYTE_LENGTH = 65;
    public static final int V_INDEX = 64;
    public static final int V_BASE = 27;
    public static final int V_LOWER_BOUND = 27;
    public static final int R_START_INDEX = 0;
    public static final int R_END_INDEX = 32;
    public static final int S_START_INDEX = 32;
    public static final int S_END_INDEX = 64;
    public static final String ADDRESS_PREFIX = "0x";
}
```
## Examples
### Generate a Random Wallet
```java
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
```
### Get Wallet Address from a Private Key
```java
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
```
### Validate if a Wallet Address is Correct
```java
/**
 * Validates whether a given Ethereum wallet address is in a correct format.
 *
 * @param address The Ethereum wallet address (should start with "0x").
 * @return True if the address is valid, false otherwise.
 */
public static boolean isValidAddress(String address) {
    return WalletUtils.isValidAddress(address);
}
```
### Validate if a Private Key is Correct
```java
/**
 * Validates whether a given private key is valid.
 *
 * @param privateKey The private key in hexadecimal format.
 * @return True if the private key is valid, false otherwise.
 */
public static boolean isValidPrivateKey(String privateKey) {
    return WalletUtils.isValidPrivateKey(privateKey);
}
```
## Sample Code
[Wallet](../../java/Wallet.java)

