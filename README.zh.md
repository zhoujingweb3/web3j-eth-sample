# web3j-eth-sample

🌍 **Languages:** [English](README.md) | [简体中文](README.zh.md)

## 项目简介
**web3j-eth-sample** 是一个基于 Java 的示例项目，展示如何使用 [web3j](https://github.com/hyperledger-web3j/web3j) 库进行以太坊区块链开发。通过该示例，开发者可以快速了解如何在 Java 项目中集成 web3j 并执行常见的以太坊操作。

## 技术栈
- **Java**
- **web3j**
- **以太坊 (Ethereum)**

## 使用方法
- 添加web3j依赖
  在 `pom.xml`（Maven）文件中添加以下依赖项：
  ```xml
  <!--web3j-->
  <dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>5.0.0</version>
  </dependency>

  <!--可选，如果不想使用org.apache.commons库，可以将示例代码中用到该库的部分替换成自己喜欢的包-->
  <dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.12.0</version>
  </dependency>
  ```
- 找到所需的功能或应用场景的示例代码
- 复制代码至您的项目
- 配置网络节点（可选）
- 运行并测试

## 功能列表
- [钱包](src/main/java/Wallet.java)
- [签名](src/main/java/Signature.java)
- [转账](src/main/java/Transfer.java)
- [合约调用](src/main/java/ContractInteraction.java)
- [链上事件监听](src/main/java/EventListener.java)

## 贡献指南
如果您有任何改进建议，欢迎提交 Issue 或 Pull Request。

## 许可证
Apache 2.0
