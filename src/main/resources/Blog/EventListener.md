# Listening to Ethereum On-Chain Events in Java

🌍 **Languages:** [English](EventListener.md) | [简体中文](EventListener.zh.md)

Web3j is a powerful tool, but it can be somewhat complex to use. Therefore, I have provided some usage examples to help you get started.

Complete example code repository: [web3j-eth-sample](https://github.com/zhoujingweb3/web3j-eth-sample)

This article provides an example of how to use Web3j in Java to listen to Ethereum smart contract events.

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
- A `setValue` function to modify the `value` and emit an event.

I have deployed this test contract on the Optimism Sepolia network at contract address: `0x833C27F4BFB4c1Eea93c747C3f5ECcf060c1B79d`. You can use it if needed. Since most public nodes block event listening APIs, you need a private RPC node. I recommend using [Alchemy](https://dashboard.alchemy.com/) to obtain a free private RPC node.

## Example
### Complete Code: `EventListener.java`
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
    private static final String CONTRACT_ADDRESS = "CONTRACT_ADDRESS";

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

        EthFilter filter = new EthFilter(
                startBlock,
                DefaultBlockParameterName.LATEST,
                CONTRACT_ADDRESS
        );

        filter.addOptionalTopics(
                EventEncoder.encode(VALUE_UPDATED)
        );

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
        if (log.getData() == null || log.getData().equals("0x")) {
            System.err.println("Log data is empty! Skipping this event.");
            return;
        }

        String updater = "0x" + log.getTopics().get(1).substring(26);
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

### Key Implementation Details
#### Defining the Event Listener
The event in the contract is defined as:
```solidity
event ValueUpdated(address indexed updater, uint256 oldValue, uint256 newValue);
```
Since `updater` is an `indexed` parameter, we must explicitly mark it as `true` in the event listener:
```java
public static final Event VALUE_UPDATED = new Event(
        "ValueUpdated",
        Arrays.asList(
                new TypeReference<Address>(true) {},
                new TypeReference<Uint256>() {},
                new TypeReference<Uint256>() {}
        )
);
```

#### Setting Up the Event Filter
You can configure the block range to listen for events. The example code listens from `EARLIEST` to `LATEST`, but you can set a specific block number:
```java
DefaultBlockParameter startBlock = DefaultBlockParameterName.EARLIEST;
EthFilter filter = new EthFilter(
        startBlock,
        DefaultBlockParameterName.LATEST,
        CONTRACT_ADDRESS
);
filter.addOptionalTopics(
        EventEncoder.encode(VALUE_UPDATED)
);
```

#### Listening for Events
```java
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

That’s it!