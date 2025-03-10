<p align="center">
    <picture>
    <source media="(prefers-color-scheme: dark)" srcset="./assets/img/logo_dark.svg">
    <source media="(prefers-color-scheme: light)" srcset="./assets/img/logo.svg">
    <img width="400" alt="Shows a black logo in light color mode and a white one in dark color mode." src="./assets/img/logo_dark.svg">
    </picture>
</p>

# NearYou - Smart Custom Advertising Platform
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
TODO:

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
