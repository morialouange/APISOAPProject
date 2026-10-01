package com.upg.legacyhr.soap;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Erreur metier : le departement demande n'existe pas dans le systeme RH.
 *
 * <p>Declenche un departement inexistant ou sans aucun employe. Le client
 * Node.js traduit ce code en HTTP 404.</p>
 *
 * <p>Trame produite :</p>
 * <pre>
 * &lt;soap:Fault&gt;
 *   &lt;faultcode&gt;DEPARTMENT_NOT_FOUND&lt;/faultcode&gt;
 *   &lt;faultstring&gt;Departement introuvable dans le systeme RH legacy.&lt;/faultstring&gt;
 *   &lt;detail&gt;
 *     &lt;DEPARTMENT_NOT_FOUND&gt;Aucun employe pour le departement 'XYZ'.&lt;/DEPARTMENT_NOT_FOUND&gt;
 *   &lt;/detail&gt;
 * &lt;/soap:Fault&gt;
 * </pre>
 */
@SoapFault(
        faultCode = FaultCode.CLIENT,
        faultStringOrReason = "Departement introuvable dans le systeme RH legacy.")
public class DepartmentNotFoundException extends HrServiceFault {

    private final String departement;

    public DepartmentNotFoundException(String departement) {
        super("Aucun employe enregistre pour le departement '" + departement + "'.");
        this.departement = departement;
    }

    public String getDepartement() {
        return departement;
    }

    @Override
    public String getFaultCode() {
        return "DEPARTMENT_NOT_FOUND";
    }
}
