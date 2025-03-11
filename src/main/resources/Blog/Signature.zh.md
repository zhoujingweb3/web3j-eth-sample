# Ethereum Message Signing and Verification in Java
🌍 **Languages:** [English](Signature.md) | [简体中文](Signature.zh.md)

# Java中如何实现以太坊消息签名及验证
Web3j是一个很好用的工具，但是使用起来有点复杂，因此我写了一些使用示例，希望可以帮到各位。
完整示例代码仓库地址：[web3j-eth-sample](https://github.com/zhoujingweb3/web3j-eth-sample)
本章主要是消息签名及验证。
## 依赖
### Maven依赖
```java
	<!--web3j-->
    <dependency>
      <groupId>org.web3j</groupId>
      <artifactId>core</artifactId>
      <version>5.0.0</version>
    </dependency>

```
### 常量类 CommonConstant.java

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
### 给消息签名

```java
	/**
     * Signs a message using a given private key and returns the signature in hexadecimal format.
     * This function applies the Ethereum-specific prefix to the message before signing.
     *
     * @param privateKeyHex The private key in hexadecimal format.
     * @param message       The message to be signed.
     * @return              The generated signature as a hexadecimal string.
     */
    public static String signPrefixedMessage(String privateKeyHex, String message) {
        BigInteger privateKey = new BigInteger(privateKeyHex, CommonConstant.PRIVATE_KEY_RADIX);

        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        Sign.SignatureData signatureData = Sign.signPrefixedMessage(message.getBytes(), keyPair);

        return Numeric.toHexStringNoPrefix(signatureData.getR()) +
                Numeric.toHexStringNoPrefix(signatureData.getS()) +
                Numeric.toHexStringNoPrefix(signatureData.getV());
    }
```

### 验证签名是否有效

```java
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
        if (signatureBytes.length != CommonConstant.SIGNATURE_BYTE_LENGTH) {
            return false;
        }

        byte v = signatureBytes[CommonConstant.V_INDEX];
        if (v < CommonConstant.V_LOWER_BOUND) {
            v += CommonConstant.V_BASE;
        }
        Sign.SignatureData signatureData = new Sign.SignatureData(
                v,
                Arrays.copyOfRange(signatureBytes, CommonConstant.R_START_INDEX, CommonConstant.R_END_INDEX),
                Arrays.copyOfRange(signatureBytes, CommonConstant.S_START_INDEX, CommonConstant.S_END_INDEX)
        );
        BigInteger publicKey;
        try {
            publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), signatureData);
        } catch (SignatureException e) {
            return false;
        }
        String parsedAddress = CommonConstant.ADDRESS_PREFIX + Keys.getAddress(publicKey);
        return parsedAddress.equalsIgnoreCase(walletAddress);
    }
```

## Sample Code
[Signature](../../java/Signature.java)