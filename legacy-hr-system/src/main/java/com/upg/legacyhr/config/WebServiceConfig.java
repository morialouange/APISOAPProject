package com.upg.legacyhr.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Configuration du service SOAP expose par le systeme legacy.
 *
 * <p>Objectif : publier le contrat {@code hr-service.xsd} et rendre le WSDL
 * telechargeable a l'adresse {@code /ws/hr-service.wsdl}, comme l'impose le
 * cahier des charges.</p>
 *
 * <p>On herite de {@link WsConfigurationSupport} plutot que de bean par bean
 * : Spring-WS enregistre alors automatiquement le routage par
 * {@code @PayloadRoot} et l'adaptateur de methode pour tous les beans
 * annotes {@code @Endpoint}. Seul le servlet doit etre declare, car
 * {@code @EnableWs} ne publie pas de servlet en Spring-WS 5.</p>
 */
@Configuration
public class WebServiceConfig extends WsConfigurationSupport {

    /**
     * Nom du bean contenant la definition du WSDL.
     *
     * <p>IMPORTANT : dans Spring-WS, le nom du bean determine directement le
     * nom du fichier WSDL servi. Avec ce nom, le contrat est disponible
     * exactement a {@code http://localhost:8080/ws/hr-service.wsdl}, comme
     * l'impose le cahier des charges ("Configurer DefaultWsdl11Definition
     * pour exposer le WSDL a l'URL /ws/mon-service.wsdl").</p>
     */
    static final String WSDL_BEAN_NAME = "hr-service";

    static final String SCHEMA_CLASSPATH = "schemas/hr-service.xsd";

    /**
     * Schema XSD compile au demarrage, utilise par Spring-WS pour valider
     * les trames SOAP entrantes ET pour serialiser la reponse sortante.
     *
     * <p>Ce bean est indispensable : sans lui, Spring-WS ne connait pas la
     * structure du message et ne peut ni valider ni serialiser la reponse.</p>
     */
    @Bean
    public XsdSchema hrSchema() {
        return new SimpleXsdSchema(new ClassPathResource(SCHEMA_CLASSPATH));
    }

    /**
     * Definition du WSDL : le bean {@code hrService} est converti en document
     * WSDL et publie sous le nom {@code /ws/hr-service.wsdl}.
     *
     * <p>C'est le nom du bean qui determine le nom du fichier WSDL.</p>
     */
    @Bean(name = WSDL_BEAN_NAME)
    public DefaultWsdl11Definition hrWsdlDefinition() {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setSchema(hrSchema());
        definition.setServiceName("HrService");
        definition.setPortTypeName("HrServicePortType");
        definition.setLocationUri("/ws");
        return definition;
    }

    /**
     * Remplace le resolveur d'exceptions par defaut afin que les
     * {@code <soap:Fault>} portent un code applicatif exploitable par le
     * client Node.js (voir {@link HrSoapFaultExceptionResolver}).
     *
     * <p>Le type de retour reste {@link SoapFaultAnnotationExceptionResolver} pour
     * rester compatible avec {@code WsConfigurationSupport} ; c'est
     * {@code resolveExceptionInternal} qui est reecrit pour produire une trame
     * {@code soap:Fault} qualifie (voir {@link HrSoapFaultExceptionResolver}).</p>
     */
    @Override
    public SoapFaultAnnotationExceptionResolver soapFaultAnnotationExceptionResolver() {
        return new HrSoapFaultExceptionResolver();
    }

    /**
     * Servlet qui publie le WSDL et distribue les trames SOAP.
     *
     * <p>Mapping sur {@code /ws/*} : le WSDL est donc accessible exactement
     * a {@code http://localhost:8080/ws/hr-service.wsdl}.</p>
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext context) {

        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        // Les <soap:Fault> sont produits par HrSoapFaultExceptionResolver et
        // serialises dans la reponse SOAP : inutile de forcer ici la
        // transformation d'erreur, l'option n'existe plus en Spring-WS 5.
        servlet.setTransformWsdlLocations(true);

        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }
}
