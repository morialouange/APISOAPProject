package com.upg.legacyhr.config;

import com.upg.legacyhr.soap.HrServiceFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultAnnotationExceptionResolver;

/**
 * Enrichit la trame {@code <soap:Fault>} du systeme RH legacy.
 *
 * <h2>Repartition des responsabilites</h2>
 * <ul>
 *   <li>le {@code faultcode} et le {@code faultstring} proviennent de
 *       l'annotation {@code @SoapFault} posee sur chaque type d'exception
 *       ({@link com.upg.legacyhr.soap.DepartmentNotFoundException},
 *       {@link com.upg.legacyhr.soap.InvalidRequestException}) ;</li>
 *   <li>cette classe ajoute dans {@code <detail>} un code metier STABLE et le
 *       message DYNAMIQUE, que le client Node exploite pour repondre a
 *       l'utilisateur.</li>
 * </ul>
 *
 * <h2>Pourquoi {@code faultcode = soap:Client} ?</h2>
 * <p>Spring-WS convertit {@code customFaultCode} via {@code QName.valueOf(...)}, qui
 * dans le JDK laisse le prefixe NON resolu :</p>
 * <pre>
 *   QName.valueOf("hr:DEPARTMENT_NOT_FOUND") -&gt; namespaceUri = ""  (vide)
 * </pre>
 * <p>SAAJ refuse alors un {@code faultcode} sans namespace
 * ({@code IllegalArgumentException: faultCode's namespaceUri cannot be empty}) et
 * toute erreur metier devenait un HTTP 500.</p>
 * <p>On applique donc la section 5 du schema SOAP 1.1 : un code applicatif est
 * porte par {@code soap:Client}, et le code metier fin dans {@code <detail>}.
 * Le contrat reste stable, le message reste lisible, et le client n'a plus a
 * deviner le code.</p>
 *
 * <h2>Trame produite</h2>
 * <pre>
 * &lt;soap:Fault&gt;
 *   &lt;faultcode&gt;soap:Client&lt;/faultcode&gt;
 *   &lt;faultstring&gt;Departement introuvable dans le systeme RH legacy.&lt;/faultstring&gt;
 *   &lt;detail&gt;
 *     &lt;hr:errorCode&gt;DEPARTMENT_NOT_FOUND&lt;/hr:errorCode&gt;
 *     &lt;hr:message&gt;Aucun employe enregistre pour le departement 'XYZ'.&lt;/hr:message&gt;
 *   &lt;/detail&gt;
 * &lt;/soap:Fault&gt;
 * </pre>
 */
public class HrSoapFaultExceptionResolver extends SoapFaultAnnotationExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(HrSoapFaultExceptionResolver.class);

    /**
     * Ajoute le code metier et le message dynamique dans {@code <detail>}.
     *
     * <p>Spring-WS appelle cette methode APRES avoir construit la fault, ce qui
     * permet d'alimenter le detail sans invalider le {@code faultcode}.</p>
     */
    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {

        // Filet de securite : une exception non prevue (panne SQL, erreur JPA)
        // ne doit jamais divulguer sa trace technique au client. Elle reste
        // tracee cote serveur, et le framework produit une fault generique.
        if (!(ex instanceof HrServiceFault hrFault)) {
            log.error("Erreur interne non prevue dans le service RH legacy", ex);
            return;
        }

        SoapFaultDetail detail = fault.addFaultDetail();
        detail.addFaultDetailElement(hrFault.getDetailName())
              .addText(hrFault.getMessage());

        log.debug("soap:Fault 'soap:Client' / hr:{} construit", hrFault.getFaultCode());
    }
}
