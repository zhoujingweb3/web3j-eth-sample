import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContractInteraction {

    private static final String RPC_URL = "RPC_URL"; // Test RPC URL, e.g., https://sepolia.optimism.io
    private static final String PRIVATE_KEY = "YOUR_PRIVATE_KEY";
    private static final String CONTRACT_ADDRESS = "CONTRACT_ADDRESS"; // Test contract address 0xE31dc4a51eFdb2675f4F3AC3cBe37097756F2913

    private static final Web3j web3j = Web3j.build(new HttpService(RPC_URL));
    private static final Credentials credentials = Credentials.create(PRIVATE_KEY);

    /**
     * Calls a view function of a smart contract (does not modify the blockchain state).
     *
     * @param functionName    The name of the contract function to call.
     * @param inputParameters The input parameters required by the function.
     * @param outputParameters The expected output types of the function.
     * @return The raw response value from the contract call.
     * @throws IOException If an error occurs while executing the request.
     */
    public static String callContract(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters) throws IOException {
        Function function = new Function(functionName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(credentials.getAddress(), CONTRACT_ADDRESS, encodedFunction),
                DefaultBlockParameterName.LATEST
        ).send();

        return response.getValue();
    }

    /**
     * Sends a transaction to execute a function on the smart contract (modifies the blockchain state).
     *
     * @param functionName    The name of the contract function to execute.
     * @param inputParameters The input parameters required by the function.
     * @param outputParameters The expected output types of the function (usually empty for transactions).
     * @return The transaction hash of the submitted transaction.
     * @throws Exception If an error occurs while sending the transaction.
     */
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

    public static void main(String[] args) throws Exception {
        // Retrieve initial contract value
        String resultBefore = callContract("getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        System.out.println("Value before transaction: " + resultBefore);

        // Construct input parameters (choose the appropriate data type from org.web3j.abi.datatypes)
        List<Type> inputParameters = new ArrayList<>();
        Uint256 value = new Uint256(BigInteger.valueOf(6));
        inputParameters.add(value);

        // Send transaction to modify the contract state
        String txHash = sendTransaction("setValue",
                inputParameters,
                Collections.emptyList());
        System.out.println("Transaction sent! Tx Hash: " + txHash);

        // Wait for the transaction to be confirmed (you can also check manually on a blockchain explorer)
        Thread.sleep(15000); // 15 seconds wait time

        // Retrieve contract value after the transaction
        String resultAfter = callContract("getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        System.out.println("Value after transaction: " + resultAfter);
    }
}
