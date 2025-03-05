# web3j-eth-sample

🌍 **Languages:** [English](README.md) | [简体中文](README.zh.md)

## Project Introduction
**web3j-eth-sample** is a Java-based sample project that demonstrates how to use the [web3j](https://github.com/hyperledger-web3j/web3j) library for Ethereum blockchain development. This example helps developers quickly understand how to integrate web3j into Java projects and perform common Ethereum operations.

## Technology Stack
- **Java**
- **web3j**
- **Ethereum**

## Usage
- Add web3j dependency
  Add the following dependencies to the `pom.xml` (Maven) file:
  ```xml
  <!-- web3j -->
  <dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>5.0.0</version>
  </dependency>

  <!-- Optional: If you do not want to use the org.apache.commons library, you can replace the parts of the sample code that use it with your preferred package. -->
  <dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.12.0</version>
  </dependency>
  ```
- Find the sample code that you need
- Copy the code into your project
- Configure network nodes (optional)
- Run and test

## Sample List
- Wallet Operations [Code](src/main/java/Wallet.java) [Doc](src/main/resources/Blog/Wallet.md)
- [Signature](src/main/java/Signature.java)
- [Transfer](src/main/java/Transfer.java)
- [ContractInteraction](src/main/java/ContractInteraction.java)
- [EventListener](src/main/java/EventListener.java)

## Contribution Guidelines
If you have any suggestions for improvements, feel free to submit an Issue or Pull Request.

## License
Apache 2.0