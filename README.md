<p align="center">
    <picture>
    <source media="(prefers-color-scheme: dark)" srcset="./assets/img/logo_dark.svg">
    <source media="(prefers-color-scheme: light)" srcset="./assets/img/logo.svg">
    <img width="400" alt="Shows a black logo in light color mode and a white one in dark color mode." src="./assets/img/logo_dark.svg">
    </picture>
</p>

# NearYou - Smart Custom Advertising Platform [![codecov](https://codecov.io/gh/klamerja/SensorFlowUNIPD/graph/badge.svg?token=7GTY9USNPC)](https://codecov.io/gh/klamerja/SensorFlowUNIPD)
Repository relativo al codice sviluppato per il capitolato C4 dell'azienda proponente Sync Lab S.r.L. per il progetto didattico di Ingegneria del Software A.A. 2024/25 del CdL Triennale in Informatica (L31) dell'Università degli Studi di Padova.

## Tecnologie adottate
Le tecnologie adottate da questo _Proof of Concept_  sono:
- Typescript per la simulazione di generazione dati (tramite API di OpenStreetMap) e per la trasmissione dati al sistema. In particolare sono state utilizzate le librerie
    - [KafkaJS](https://kafka.js.org/) per la trasmissione dati alla _queue_ di Kafka servita dal sistema
    - [Mapbox Polyline](https://www.npmjs.com/package/@mapbox/polyline) per la decodifica dei dati in coppie [latitudine, longitudine]
- Java, in particolare [Spring Boot](https://spring.io/projects/spring-boot) per sviluppare la parte di stream processing del sistema, ovvero la raccolta dei dati inviati tramite Kafka, la loro persistenza in DB e la generazione degli annunci a seconda della posizione del sensore
- PostgreSQL e il plugin PostGIS per la persistenza dei dati e per le operazioni di incrocio geospaziale.
- Grafana come dashboard del sistema

## Come avviare e utilizzare il sistema
1. Buildare l'immagine Docker del backend in Spring
    ```sh
    docker build -t spring-backend .
    ```
2. Buildare l'immagine Docker del simulatore (posizionandosi nella cartella `client` prima di tutto)
    ```sh
    docker build -t simulator .
    ```
3. Avviare il sistema tramite il Docker Compose
    ```sh
    docker compose up -d
    ```
4. Per accedere alla dashboard, collegarsi all'indirizzo [localhost:3000](http://localhost:3000) oppure [127.0.0.1:3000](http://127.0.0.1:3000) con lo username `admin` e la password `admin` (il sistema chiederà di cambiare la password in quanto è stata mantenuta la default di Grafana, questo passaggio è skippabile).

## Componenti del gruppo

| Nominativo        | Matricola |
| :---------------- | :-------: |
| Andrea Perozzo    |  2082849  |
| Andrea Precoma    |  2068227  |
| Davide Marin      |  2068234  |
| Davide Martinelli |  2034341  |
| Davide Picello    |  2034825  |
| Klaudio Merja     |  2075538  |
| Riccardo Milan    |  2068231  |
