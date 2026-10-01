package com.upg.legacyhr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree du systeme legacy "legacy-hr-system".
 *
 * <p>Ce micro-service Spring Boot joue le role de SERVEUR SOAP dans le
 * scenario d'integration heterogene de l'examen : il expose, via un contrat
 * WSDL/XSD, les employes et leurs competences du systeme RH historique.</p>
 *
 * <p>Il ne propose aucune interface graphique : uniquement le backend SOAP
 * destine a etre consomme par l'application Node.js / MongoDB.</p>
 *
 * <p>WSDL : http://localhost:8080/ws/hr-service.wsdl</p>
 */
@SpringBootApplication
public class LegacyHrSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyHrSystemApplication.class, args);
    }
}
