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

    private static final String RPC_URL = "RPC_URL"; // test rpc url https://sepolia.optimism.io
    private static final String PRIVATE_KEY = "YOUR_PRIVATE_KEY";
    private static final String CONTRACT_ADDRESS = "CONTRACT_ADDRESS"; // test contract address 0xE31dc4a51eFdb2675f4F3AC3cBe37097756F2913

    private static final Web3j web3j = Web3j.build(new HttpService(RPC_URL));
    private static final Credentials credentials = Credentials.create(PRIVATE_KEY);

    /**
     * 调用智能合约的 view 方法 (不会修改状态)
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
     * 调用智能合约的 set 方法 (会修改状态)
     */
    public static String sendTransaction(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters) throws Exception {
        // 构造合约调用方法
        Function function = new Function(functionName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);

        // 获取 Chain ID
        EthChainId chainIdResponse = web3j.ethChainId().send();
        BigInteger chainId = chainIdResponse.getChainId();

        // 使用 RawTransactionManager 发送交易
        RawTransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId.longValue());

        EthSendTransaction transactionResponse = transactionManager.sendTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                CONTRACT_ADDRESS,
                encodedFunction,
                BigInteger.ZERO
        );

        // 检查交易是否有错误
        if (transactionResponse.hasError()) {
            throw new RuntimeException("Error sending transaction: " + transactionResponse.getError().getMessage());
        }

        return transactionResponse.getTransactionHash();
    }

    public static void main(String[] args) throws Exception {
        // 读取合约初始值
        String resultBefore = callContract("getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        System.out.println("Value before transaction: " + resultBefore);

        // 构建输入参数，你如果想要传递更多类型的参数，请选择org.web3j.abi.datatypes中的类型作为参数类型
        List<Type> inputParameters = new ArrayList<>();
        Uint256 value = new Uint256(BigInteger.valueOf(6));
        inputParameters.add(value);
        // 发送交易，修改值为 value
        String txHash = sendTransaction("setValue",
                inputParameters,
                Collections.emptyList());
        System.out.println("Transaction sent! Tx Hash: " + txHash);

        // 等待交易确认（可以手动检查区块链浏览器）
        Thread.sleep(15000); // 15 秒等待时间

        // 读取合约值
        String resultAfter = callContract("getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        System.out.println("Value after transaction: " + resultAfter);
    }
}
