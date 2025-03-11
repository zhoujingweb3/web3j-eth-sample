# Java中调用以太坊智能合约

🌍 **Languages:** [English](ContractInteraction.md) | [简体中文](ContractInteraction.zh.md)

Web3j是一个很好用的工具，但是使用起来有点复杂，因此我写了一些使用示例，希望可以帮到各位。
完整示例代码仓库地址：[web3j-eth-sample](https://github.com/zhoujingweb3/web3j-eth-sample)
本章内容是关于使用Web3j在Java中调用以太坊智能合约的示例。
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
### 测试合约代码TestContract.sol

```java
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract TestContract {
    uint256 private value;

    event ValueUpdated(address indexed updater, uint256 oldValue, uint256 newValue);

    // 设置初始值
    constructor(uint256 _initialValue) {
        value = _initialValue;
    }

    // 读取数值 (view, 只读查询)
    function getValue() public view returns (uint256) {
        return value;
    }

    // 修改数值 (需要交易 & Gas)
    function setValue(uint256 _newValue) public {
        uint256 oldValue = value;
        value = _newValue;
        emit ValueUpdated(msg.sender, oldValue, _newValue);
    }
}

```
这个测试合约包含了一个uint256形value变量，一个getValue函数获取value变量的值，一个setValue函数设置value变量的值。
我在optimism链的sepolia网络部署了该测试合约，合约地址为：0x833C27F4BFB4c1Eea93c747C3f5ECcf060c1B79d，有需要的话可以直接使用，RPC_URL参数也可以用公用的RPC节点URL，例如：https://sepolia.optimism.io
## 示例
### 调用只读函数（只获取链上数据，不需要Gas）
```java
    public static String callContract(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters) throws IOException {
        Function function = new Function(functionName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(credentials.getAddress(), CONTRACT_ADDRESS, encodedFunction),
                DefaultBlockParameterName.LATEST
        ).send();

        return response.getValue();
    }
```
### 调用写入函数（写入或更改链上数据，需要Gas）
```java
    public static String sendTransaction(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters) throws Exception {
        // Construct the function call
        Function function = new Function(functionName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);

        // Retrieve Chain ID
        EthChainId chainIdResponse = web3j.ethChainId().send();
        BigInteger chainId = chainIdResponse.getChainId();

        // Use RawTransactionManager to send the transaction
        RawTransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId.longValue());

        EthSendTransaction transactionResponse = transactionManager.sendTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                CONTRACT_ADDRESS,
                encodedFunction,
                BigInteger.ZERO
        );

        // Check for transaction errors
        if (transactionResponse.hasError()) {
            throw new RuntimeException("Error sending transaction: " + transactionResponse.getError().getMessage());
        }

        return transactionResponse.getTransactionHash();
    }
```
## 示例代码
[ContractInteraction](../../java/ContractInteraction.java)

在示例代码的Main函数中，我们实现了以下测试步骤：

1. 调用合约的getValue函数，读取 Value 的值
2. 调用合约的setValue函数，更改 Value 的值
3. 等待15秒中后，再次调用合约的getValue函数，读取 Value 的值，确认修改成功

以上。