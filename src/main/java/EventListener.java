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
