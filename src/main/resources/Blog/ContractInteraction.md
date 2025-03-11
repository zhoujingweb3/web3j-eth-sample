# Calling Ethereum Smart Contracts in Java

🌍 **Languages:** [English](ContractInteraction.md) | [简体中文](ContractInteraction.zh.md)

Web3j is a powerful tool, but it can be somewhat complex to use. Therefore, I have provided some usage examples to help you get started.

Complete example code repository: [web3j-eth-sample](https://github.com/zhoujingweb3/web3j-eth-sample)

This article provides examples of using Web3j in Java to interact with Ethereum smart contracts.

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

### Test Smart Contract Code: `TestContract.sol`
```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract TestContract {
    uint256 private value;

    event ValueUpdated(address indexed updater, uint256 oldValue, uint256 newValue);

    // Set initial value
    constructor(uint256 _initialValue) {
        value = _initialValue;
    }

    // Read the value (view, read-only)
    function getValue() public view returns (uint256) {
        return value;
    }

    // Update the value (requires transaction & gas)
    function setValue(uint256 _newValue) public {
        uint256 oldValue = value;
        value = _newValue;
        emit ValueUpdated(msg.sender, oldValue, _newValue);
    }
}
```
This test contract contains:
- A `uint256` variable `value`.
- A `getValue` function to retrieve the `value`.
- A `setValue` function to modify the `value`.

I have deployed this test contract on the Optimism Sepolia network at contract address: `0x833C27F4BFB4c1Eea93c747C3f5ECcf060c1B79d`. You can use it if needed. The `RPC_URL` parameter can also be set to a public RPC node URL, such as `https://sepolia.optimism.io`.

## Examples
### Calling a Read-Only Function (Fetching On-Chain Data Without Gas)
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
### Calling a Write Function (Modifying On-Chain Data, Requires Gas)
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

## Example Code
[ContractInteraction](../../java/ContractInteraction.java)

### Test Steps in the `main` Function
In the provided example, the following steps are executed:
1. Call the `getValue` function of the contract to read the current `value`.
2. Call the `setValue` function of the contract to update the `value`.
3. Wait for `15 seconds`.
4. Call `getValue` again to confirm the update was successful.

That's it!