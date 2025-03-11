# How to Transfer ETH in Java

🌍 **Languages:** [English](Transfer.md) | [简体中文](Transfer.zh.md)

Web3j is a powerful tool, but it can be somewhat complex to use. Therefore, I have provided some usage examples to help you get started.

Complete example code repository: [web3j-eth-sample](https://github.com/zhoujingweb3/web3j-eth-sample)

This article covers how to use Web3j in Java to check Ethereum balances and transfer ETH.

## Dependencies
### Maven Dependency
```xml
<!--web3j-->
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>5.0.0</version>
</dependency>
```

## Examples
### Checking Balance

```java
/**
 * Retrieves the Ether balance of the given Ethereum address.
 *
 * @param address The Ethereum address whose balance is to be retrieved.
 * @return The balance in Ether as a BigDecimal.
 * @throws IOException If there is an issue communicating with the Ethereum node.
 */
public static BigDecimal getETHBalance(String address) throws IOException {
    // Retrieve balance in Wei
    BigInteger balanceInWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
    // Convert Wei to Ether
    return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);
}
```

### Transfer ETH
```java
/**
 * Transfers Ether from a sender's account to a recipient's account.
 *
 * @param senderPrivateKey The private key of the sender's Ethereum account (keep this secure).
 * @param recipientAddress The recipient's Ethereum address.
 * @param amountInEther    The amount to transfer in Ether.
 * @return The transaction hash if the transfer is successful; otherwise, returns null.
 * @throws IOException If there is an issue communicating with the Ethereum node.
 */
public static String transfer(String senderPrivateKey, String recipientAddress, BigDecimal amountInEther) throws IOException {
    // Retrieve the chain ID of the connected network
    EthChainId chainIdResponse = web3j.ethChainId().send();
    long chainId = chainIdResponse.getChainId().longValue();

    /* Sender Account Configuration */
    // Create credentials from the sender's private key
    Credentials credentials = Credentials.create(senderPrivateKey);
    String senderAddress = credentials.getAddress();
    // Get the current nonce for the sender's account
    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
            senderAddress, DefaultBlockParameterName.LATEST).send();
    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

    /* Transaction Amount and Fee Configuration */
    // Convert the amount from Ether to Wei (1 ETH = 10^18 Wei)
    BigInteger value = Convert.toWei(amountInEther, Convert.Unit.ETHER).toBigInteger();
    // Get the current Gas price
    EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
    BigInteger gasPrice = ethGasPrice.getGasPrice();
    // Gas limit for a standard Ether transfer (typically 21,000)
    BigInteger gasLimit = BigInteger.valueOf(21000);

    /* Transfer */
    // Create the transaction object
    RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
            nonce, gasPrice, gasLimit, recipientAddress, value);
    // Sign the transaction
    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
    String hexValue = Numeric.toHexString(signedMessage);
    // Send the transaction
    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
    // Return the transaction hash if successful, or null if the transaction failed
    return ethSendTransaction.getTransactionHash();
}
```

Since this code is a little bit complex, let’s analyze it step by step.

### Retrieving Network Node Information
```java
// Retrieve the chain ID of the connected network
EthChainId chainIdResponse = web3j.ethChainId().send();
long chainId = chainIdResponse.getChainId().longValue();
```
According to the EIP-155 standard, to prevent replay attacks, we need to include the `chainId` as a parameter when signing transactions. Therefore, we must retrieve the `chainId` of the RPC node.

In practice, this operation is usually performed once and then reused.

### Configuring the Sender’s Account
```java
/* Sender Account Configuration */
// Create credentials from the sender's private key
Credentials credentials = Credentials.create(senderPrivateKey);
String senderAddress = credentials.getAddress();
// Get the current nonce for the sender's account
EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
        senderAddress, DefaultBlockParameterName.LATEST).send();
BigInteger nonce = ethGetTransactionCount.getTransactionCount();
```

### Setting Transaction Gas
```java
/* Transaction Amount and Fee Configuration */
// Convert the amount from Ether to Wei (1 ETH = 10^18 Wei)
BigInteger value = Convert.toWei(amountInEther, Convert.Unit.ETHER).toBigInteger();
// Get the current Gas price
EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
BigInteger gasPrice = ethGasPrice.getGasPrice();
// Gas limit for a standard Ether transfer (typically 21,000)
BigInteger gasLimit = BigInteger.valueOf(21000);
```
We typically set `gasPrice` to the current real-time gas consumption value. In real-world applications, you may adjust `gasPrice` dynamically.

For a standard Ether transfer, the gas consumption limit is usually `21000`.

### Executing the Transfer
```java
/* Transfer */
// Create the transaction object
RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
        nonce, gasPrice, gasLimit, recipientAddress, value);
// Sign the transaction
byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
String hexValue = Numeric.toHexString(signedMessage);
// Send the transaction
EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
// Return the transaction hash if successful, or null if the transaction failed
return ethSendTransaction.getTransactionHash();
```

### Complete Code: `Transfer.java`
[Transfer.java](../../java/Transfer.java)

In the `main` function, we first check the recipient’s balance, then transfer `0.001 ETH`, wait for `15 seconds`, and check the balance again. Under normal conditions, the recipient’s address should increase by `0.001 ETH`.

