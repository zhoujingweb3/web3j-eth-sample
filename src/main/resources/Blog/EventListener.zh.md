# Java中监听以太坊链上事件

🌍 **Languages:** [English](EventListener.md) | [简体中文](EventListener.zh.md)

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
我在optimism链的sepolia网络部署了该测试合约，合约地址为：0x833C27F4BFB4c1Eea93c747C3f5ECcf060c1B79d，有需要的话可以直接使用，由于一般的公用节点把链上事件监听方法给屏蔽了，因此这里需要使用私人节点，推荐使用[https://dashboard.alchemy.com/](https://dashboard.alchemy.com/) 获取免费的私人节点，节点获取方式如下图。
![私人RPC节点URL](https://i-blog.csdnimg.cn/direct/15271f8f68f540e8b85374e4f19de6ad.png)
## 示例
### 完整代码 EventListener.java
[EventListener](../../java/EventListener.java)

```java
import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class EventListener {

    private static final String RPC_URL = "RPC_URL";
    // Since most public rpc node has block the event api, so you should set your own rpc node url.
    // You can get a free usage rpc node from https://dashboard.alchemy.com/

    private static final String CONTRACT_ADDRESS = "CONTRACT_ADDRESS"; // Test contract address 0x833C27F4BFB4c1Eea93c747C3f5ECcf060c1B79d

    public static final Event VALUE_UPDATED = new Event(
            "ValueUpdated",
            Arrays.asList(
                    new TypeReference<Address>(true) {}, // indexed `updater`
                    new TypeReference<Uint256>() {}, // oldValue (non-indexed)
                    new TypeReference<Uint256>() {}  // newValue (non-indexed)
            )
    );

    private static final Web3j web3j = Web3j.build(new HttpService(RPC_URL));

    public void startLogListening() {
        DefaultBlockParameter startBlock = DefaultBlockParameterName.EARLIEST;
        // If you want to start from a specific block, you can set the block number as below
        // startBlock = DefaultBlockParameter.valueOf(blockNumber);

        EthFilter filter = new EthFilter(
                startBlock,
                DefaultBlockParameterName.LATEST,  // You can change this value to set the latest block you want to listen to
                CONTRACT_ADDRESS
        );

        // Add event encoding to the filter
        filter.addOptionalTopics(
                EventEncoder.encode(VALUE_UPDATED)
        );

        // Listen to event logs
        Flowable<Log> logFlowable = web3j.ethLogFlowable(filter);

        logFlowable.subscribe(
                log -> {
                    String eventSignature = log.getTopics().get(0);
                    if (eventSignature.equals(EventEncoder.encode(VALUE_UPDATED))) {
                        valueUpdated(log);
                    }
                },
                throwable -> {
                    System.err.println("Error processing event log: " + throwable.getMessage());
                }
        );
    }

    private void valueUpdated(Log log) {
        // Ensure log data is not empty
        if (log.getData() == null || log.getData().equals("0x")) {
            System.err.println("Log data is empty! Skipping this event.");
            return;
        }

        // Retrieve `updater` (indexed parameter stored in topics[1])
        String updater = "0x" + log.getTopics().get(1).substring(26); // Extract the last 40 characters of the address

        // Decode non-indexed parameters `oldValue` and `newValue`
        List<Type> decoded = FunctionReturnDecoder.decode(log.getData(), VALUE_UPDATED.getNonIndexedParameters());

        if (decoded.size() < 2) {
            System.err.println("Decoded data size is incorrect!");
            return;
        }

        int oldValue = ((BigInteger) decoded.get(0).getValue()).intValue();
        int newValue = ((BigInteger) decoded.get(1).getValue()).intValue();

        System.out.println("Value updated by " + updater + ", old: " + oldValue + ", new: " + newValue);
    }

    public static void main(String[] args) {
        EventListener eventListener = new EventListener();
        eventListener.startLogListening();
    }
}

```
在上述代码中，我们首先要定义监听事件，这里需要特别注意，在合约代码中事件的定义`event ValueUpdated(address indexed updater, uint256 oldValue, uint256 newValue);`，其中updater是indexed，**所以需要给一个初始化true值**，其他的则不需要。
```java
	public static final Event VALUE_UPDATED = new Event(
            "ValueUpdated",
            Arrays.asList(
                    new TypeReference<Address>(true) {}, // indexed `updater`
                    new TypeReference<Uint256>() {}, // oldValue (non-indexed)
                    new TypeReference<Uint256>() {}  // newValue (non-indexed)
            )
    );
```
然后设置需要监听的事件过滤，这里需要配置你想要监听的区块范围，示例代码中设置的范围是从EARLIEST到LATEST，可以通过`DefaultBlockParameter.valueOf(blockNumber);`来设置区块号。

```java
		DefaultBlockParameter startBlock = DefaultBlockParameterName.EARLIEST;
        // If you want to start from a specific block, you can set the block number as below
        // startBlock = DefaultBlockParameter.valueOf(blockNumber);

        EthFilter filter = new EthFilter(
                startBlock,
                DefaultBlockParameterName.LATEST,  // You can change this value to set the latest block you want to listen to
                CONTRACT_ADDRESS
        );

        // Add event encoding to the filter
        filter.addOptionalTopics(
                EventEncoder.encode(VALUE_UPDATED)
        );
```
开始进行监听，通过`log.getTopics().get(0)`来获取事件名称：

```java
		// Listen to event logs
        Flowable<Log> logFlowable = web3j.ethLogFlowable(filter);

        logFlowable.subscribe(
                log -> {
                    String eventSignature = log.getTopics().get(0);
                    if (eventSignature.equals(EventEncoder.encode(VALUE_UPDATED))) {
                        valueUpdated(log);
                    }
                },
                throwable -> {
                    System.err.println("Error processing event log: " + throwable.getMessage());
                }
        );
```
解析事件：

```java
	private void valueUpdated(Log log) {
        // Ensure log data is not empty
        if (log.getData() == null || log.getData().equals("0x")) {
            System.err.println("Log data is empty! Skipping this event.");
            return;
        }

        // Retrieve `updater` (indexed parameter stored in topics[1])
        String updater = "0x" + log.getTopics().get(1).substring(26); // Extract the last 40 characters of the address

        // Decode non-indexed parameters `oldValue` and `newValue`
        List<Type> decoded = FunctionReturnDecoder.decode(log.getData(), VALUE_UPDATED.getNonIndexedParameters());

        if (decoded.size() < 2) {
            System.err.println("Decoded data size is incorrect!");
            return;
        }

        int oldValue = ((BigInteger) decoded.get(0).getValue()).intValue();
        int newValue = ((BigInteger) decoded.get(1).getValue()).intValue();

        System.out.println("Value updated by " + updater + ", old: " + oldValue + ", new: " + newValue);
    }

```
以上。