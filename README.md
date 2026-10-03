# legacy-hr-system

[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![SOAP](https://img.shields.io/badge/soap-1.1%20document%2Fliteral-2c5aa0.svg)](https://www.w3.org/TR/SOAP/)
[![Maven](https://img.shields.io/badge/maven-3.9%2B-blue.svg)](https://maven.apache.org/)

Service SOAP de gestion des Ressources Humaines.

Expose les employés, leurs compétences et leurs départements au format
**SOAP 1.1 / document-literal**, à partir d'un contrat WSDL généré depuis un
schéma XSD unique.

> **Ce dépôt est le service *legacy*.** Il n'a aucune interface web : il se consomme
> exclusivement en SOAP, par exemple depuis l'application **GTPweb** (Node.js · Express ·
> MongoDB) qui réplique ces données.

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Démarrage rapide](#démarrage-rapide)
- [Appeler le service](#appeler-le-service)
- [Référence de l'API](#référence-de-lapi)
- [Modèle de données](#modèle-de-données)
- [Configuration](#configuration)
- [Tests](#tests)
- [Structure du projet](#structure-du-projet)

---

## Fonctionnalités

- **Contrat WSDL auto-généré** à partir du XSD, publié à `/ws/hr-service.wsdl`
- **Deux opérations** : lister les employés d'un département, lister les départements
- **Listes imbriquées** : chaque employé expose ses compétences dans la réponse XML
- **Énumérations typées** pour la disponibilité et le niveau de compétence
- **Codes d'erreur SOAP stables**, exploitables par le client sans parser le message
- **Base H2 en mémoire** : aucune installation, données rechargées à chaque démarrage
- **Génération des classes JAXB** depuis le XSD à la compilation — aucun binding écrit à la main

---

## Stack technique

| Composant | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.1 |
| Spring Web Services | *managed* |
| Spring Data JPA | *managed* |
| H2 | *managed* |
| JAXB (`jaxb2-maven-plugin`) | 3.2.0 |
| wsdl4j | 1.6.3 |

---

## Démarrage rapide

### Prérequis

| Outil | Version | Vérification |
|---|---|---|
| JDK | 21 | `java -version` |
| Maven | ≥ 3.9 | `mvn -v` |

### Compiler et lancer

```bash
git clone https://github.com/morialouange/APISOAPProject.git
cd APISOAPProject/legacy-hr-system
mvn clean package
java -jar target/legacy-hr-system-1.0.0.jar
```

### Lancer en arrière-plan (Windows)

```cmd
demarrer-legacy-hr.bat
```

Le script arrête une éventuelle instance déjà lancée sur le port 8080, démarre le
JAR puis interroge le WSDL en boucle jusqu'à ce qu'il réponde, et journalise dans
`legacy-hr-system.log`.

### Vérifier que le service tourne

```bash
curl -o /dev/null -w "%{http_code}\n" http://localhost:8080/ws/hr-service.wsdl
# attendu : 200
```

| URL | Description |
|---|---|
| <http://localhost:8080/ws/hr-service.wsdl> | contrat WSDL |
| <http://localhost:8080/ws/hr-service.xsd> | schéma XSD |
| <http://localhost:8080/h2-console> | console H2 |

---

## Appeler le service

### Lister les employés d'un département

```bash
curl -X POST http://localhost:8080/ws \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
       xmlns:hr="http://upg.ac.rw/soap/hr">
      <soapenv:Body>
        <hr:getDepartmentEmployeesRequest>
          <hr:department>IT</hr:department>
        </hr:getDepartmentEmployeesRequest>
      </soapenv:Body>
    </soapenv:Envelope>'
```

### Lister les départements

```bash
curl -X POST http://localhost:8080/ws \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
       xmlns:hr="http://upg.ac.rw/soap/hr">
      <soapenv:Body>
        <hr:getAllDepartmentsRequest/>
      </soapenv:Body>
    </soapenv:Envelope>'
```

### Erreurs

Un département inexistant ne renvoie pas une liste vide mais une `soap:Fault`
portant un code métier stable :

| Code | Déclencheur |
|---|---|
| `DEPARTMENT_NOT_FOUND` | le département demandé n'existe pas |
| `INVALID_REQUEST` | le paramètre `department` est absent ou vide |

Le code métier est porté par le **nom de l'élément** `<detail>`, ce qui permet au
client de l'identifier sans dépendre du message humain, susceptible d'être traduit.
Le `faultcode` reste `soap:Client`, conformément à la section 5 du schéma SOAP 1.1.

> La forme exacte des réponses est décrite dans
> [Référence de l'API](#référence-de-lapi). Les réponses réelles, capturées et
> contrôlées par assertions, sont dans la collection SoapUI
> (`soapui/legacy-hr-system.xml`).

---

## Référence de l'API

| | |
|---|---|
| **Namespace** | `http://upg.ac.rw/soap/hr` |
| **Endpoint** | `http://localhost:8080/ws` |
| **Binding** | SOAP 1.1 — *document / literal* |
| **WSDL** | `http://localhost:8080/ws/hr-service.wsdl` |

### Opérations

| Opération | Paramètre | Renvoie |
|---|---|---|
| `getDepartmentEmployees` | `department` (`string`) | `employees` + `totalCount` |
| `getAllDepartments` | *(aucun)* | `departments` (code + effectif) |

### Types complexes

| Type | Champs |
|---|---|
| `hr:Employee` | `id`, `employeeCode`, `firstName`, `lastName`, `fullName`, `department`, `availabilityStatus`, `skills` |
| `hr:SkillList` | `skill` *(0..n)* |
| `hr:Skill` | `skillName`, `proficiency` |
| `hr:EmployeeList` | `employee` *(0..n)* |
| `hr:DepartmentList` | `department` *(0..n)* |
| `hr:DepartmentInfo` | `code`, `employeeCount` |

> `fullName` est **dérivé** de `firstName` et `lastName`, et n'est pas stocké en base.

### Énumérations

| Énumération | Valeurs |
|---|---|
| `hr:AvailabilityStatus` | `AVAILABLE` · `ON_LEAVE` · `BUSY` |
| `hr:Proficiency` | `EXPERT` · `INTERMEDIATE` · `BEGINNER` |

---

## Modèle de données

Le schéma XML reflète directement la base relationnelle. `employee_skill` est une
table fille, ce qui produit une liste de compétences imbriquée dans chaque employé.

| Table | Colonne | Élément XML |
|---|---|---|
| `employee` | `id` | `id` |
| | `employee_code` | `employeeCode` |
| | `first_name` | `firstName` |
| | `last_name` | `lastName` |
| | `department` | `department` |
| | `status` | `availabilityStatus` |
| `employee_skill` | `skill_name` | `skills/skill/skillName` |
| | `proficiency` | `skills/skill/proficiency` |

Le schéma fait foi sur la base : `src/main/resources/schemas/hr-service.xsd`.
Les classes Java du binding sont générées à la compilation depuis ce fichier.

---

## Configuration

`legacy-hr-system/src/main/resources/application.properties`

| Propriété | Valeur | Rôle |
|---|---|---|
| `server.port` | `8080` | port d'écoute |
| `spring.datasource.url` | `jdbc:h2:mem:legacyhr` | base H2 **en mémoire** |
| `spring.jpa.hibernate.ddl-auto` | `create` | schéma généré depuis les entités |
| `spring.jpa.defer-datasource-initialization` | `true` | joue `data.sql` **après** la création du schéma |
| `spring.h2.console.enabled` | `true` | console H2 sur `/h2-console` |
| `logging.level.com.upg.legacyhr` | `DEBUG` | journalisation du service |

> **Pour une base persistante**, remplacer l'URL par `jdbc:h2:file:./data/hrdb`.
> Sans `defer-datasource-initialization=true`, `data.sql` est exécuté avant que
> Hibernate ne crée les tables et les insertions échouent.

---

## Tests

Une collection SoapUI prête à l'emploi est fournie : `soapui/legacy-hr-system.xml`.

```
legacy-hr-system
└── HrServicePortType
    ├── getDepartmentEmployees
    │     ├── IT - 4 employes, 12 competences
    │     ├── MARKETING - 3 employes
    │     └── INCONNU - soap:Fault
    └── getAllDepartments
          └── Tous les departements
```

Elle contient **4 requêtes** et **16 assertions XPath**, qui vérifient notamment
le nombre d'employés et le nombre de compétences renvoyés.

1. Démarrer le service
2. SoapUI → **File → Open Project…** → `soapui/legacy-hr-system.xml`
   (*SoapUI Project*, pas *WSDL*)
3. Double-cliquer sur une opération, choisir la requête, bouton **vert ▶**

> N'enregistrez pas le projet à la fermeture de SoapUI : le fichier serait
> réécrit et les assertions perdues.

Le dossier `soapui/` contient également le contrat en copie de référence
(`hr-service.wsdl`, `hr-service.xsd`).

```bash
mvn test
```

---

## Structure du projet

```
ApiSOAPProject/
├── demarrer-legacy-hr.bat          démarrage en arrière-plan (Windows)
├── hr-service.wsdl                contrat publié par le serveur
├── soapui/
│   ├── legacy-hr-system.xml       collection SoapUI
│   ├── hr-service.wsdl
│   └── hr-service.xsd
└── legacy-hr-system/
    ├── pom.xml
    └── src/main/
        ├── java/com/upg/legacyhr/
        │   ├── LegacyHrSystemApplication.java
        │   ├── config/
        │   │   ├── WebServiceConfig.java                 publication du WSDL
        │   │   └── HrSoapFaultExceptionResolver.java     soap:Fault
        │   ├── model/                                   entités JPA
        │   │   ├── Employee.java
        │   │   ├── EmployeeSkill.java
        │   │   ├── AvailabilityStatus.java
        │   │   └── Proficiency.java
        │   ├── repository/
        │   │   └── EmployeeRepository.java
        │   └── soap/
        │       ├── HrServiceEndpoint.java                opérations
        │       ├── EmployeeSoapMapper.java               mapping JPA → JAXB
        │       ├── HrServiceFault.java
        │       ├── DepartmentNotFoundException.java
        │       └── InvalidRequestException.java
        └── resources/
            ├── application.properties
            ├── data.sql
            └── schemas/
                └── hr-service.xsd                        le contrat
```