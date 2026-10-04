# wallet-service

Java 21 / Spring Boot 3.3.5 wallet API.

Standalone one-command startup: `docker compose up --build -d` from this directory.

## Run from IntelliJ
1. Start PostgreSQL and Kafka using the root `docker-compose.yml`: `docker compose up -d postgres kafka`.
2. Import the root Maven project or this `pom.xml`.
3. Run `WalletServiceApplication`.

Defaults: PostgreSQL `localhost:5432/walletdb` (`wallet/wallet`), Kafka `localhost:9092`, HTTP `8080`.

## Tests
`mvn test`

The automated concurrency scripts are in the root `scripts/` folder.
