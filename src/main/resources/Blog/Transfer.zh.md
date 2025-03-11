# Java中如何转账发送ETH
🌍 **Languages:** [English](Transfer.md) | [简体中文](Transfer.zh.md)

Web3j是一个很好用的工具，但是使用起来有点复杂，因此我写了一些使用示例，希望可以帮到各位。
完整示例代码仓库地址：[web3j-eth-sample](https://github.com/zhoujingweb3/web3j-eth-sample)
本章内容是如何使用Web3j在Java是进行以太坊余额查询和转账，发送ETH。
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
## 示例
### 查询余额

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

### 转账，发送ETH
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
由于这段代码较长，现在我们逐段分析。
#### 获取网络节点信息

```java
// Retrieve the chain ID of the connected network
EthChainId chainIdResponse = web3j.ethChainId().send();
long chainId = chainIdResponse.getChainId().longValue();
```
基于EIP155规范，为了防御重放攻击，我们需要将chainId作为交易签名的参数之一，因此，我们还需要获取RPC节点对应的chainId。
工程实践中，我们通常只需要完成一次上述操作，然后复用chainId。
#### 设置转账账户信息
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
#### 设置交易gas
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
通常我们设置 gasPrice 为当前实时gas消耗值，考虑到实际应用，你可以动态的增加或减少 gasPrice。
在一次转账中，通常gas消耗上限值为21000。
#### 执行转账，发送ETH至目标地址
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

## 示例代码
[Transfer](../../java/Transfer.java)

在示例代码的，Main函数中，我们首先查询了收款人地址余额，然后向收款人地址转账0.001ETH，等待15秒钟后，再次查询收款人地址余额，正常情况下，我们会发现收款人地址增加了0.001ETH。