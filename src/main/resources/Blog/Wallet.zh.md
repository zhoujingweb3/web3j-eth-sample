# Java中操作以太坊钱包
🌍 **Languages:** [English](Wallet.md) | [简体中文](Wallet.zh.md)

Web3j是一个很好用的工具，但是使用起来有点复杂，因此我写了一些使用示例，希望可以帮到各位。
本章主要钱包的相关操作。
## 依赖
### Maven依赖
```
	<!--web3j-->
    <dependency>
      <groupId>org.web3j</groupId>
      <artifactId>core</artifactId>
      <version>5.0.0</version>
    </dependency>
```
###  常量类 CommonConstant.java

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

## 示例

### 生成随机钱包

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

### 根据私钥获取钱包地址

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
### 判断钱包地址是否合法

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

###  判断私钥是否合法

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
## 示例代码
[Wallet](../../java/Wallet.java)