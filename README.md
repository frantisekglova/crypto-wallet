# Crypto Wallet Simulator

CryptoCurrency wallet simulator is application for simulation currency wallets. App allows CRUD operations for wallets and allows transfer operation
within the wallet and among the wallets.

## Technologies

- Apache Maven 3.6.3
- Java 8 (AdoptOpenJDK)
- Spring Boot 2.4.4
- Lombok (to unclutter the code)
- H2 (in memory DB)
- Swagger (UI for Rest API)

## How to run locally

Go to folder where you want to clone app from a repository:

```
cd {absolute_path_to_folder}
```

Clone repository into given folder:

```
git clone https://github.com/frantisekglova/crypto-wallet
```

Change application.yaml server port property to set port you wish application runs on:

```yaml
server:
  port: 8080
```

You can continue with two option: 

### Run installed app:

```
mvn clean install
```

Open created target folder:

```
cd target
```

Run created jar file:

```
java -jar crypto-wallet-1.0.0.jar
```

Stop a jar

```
ctrl + C
```

### Run app with Maven

```
mvn spring-boot:run
```

## How to deploy

Deployment is automatic. After pushing to a main branch in a repository, Heroku start automatic deploy of a newer version of program.

## Logging

Heroku provide request logging as part of their service, and you do not need to implement any logging.

## How to run deployed app

Go to given [url](https://crypto-wallet-simulator.herokuapp.com/ "crypto-wallet-simulator app"). You will be redirected into swagger UI, so you can
see all available endpoints with a documentation.

## Author

Frantisek Glova (frantisek.glova@hotovo.com) - software developer