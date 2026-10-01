package com.upg.legacyhr.soap;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Erreur metier : la requete SOAP est incomplete ou invalide.
 *
 * <p>Declenche lorsque {@code <department/>} est absent ou vide. Le client
 * Node.js traduit ce code en HTTP 400.</p>
 */
@SoapFault(
        faultCode = FaultCode.CLIENT,
        faultStringOrReason = "Requete SOAP invalide : parametre obligatoire manquant.")
public class InvalidRequestException extends HrServiceFault {

    public InvalidRequestException(String message) {
        super(message);
    }

    @Override
    public String getFaultCode() {
        return "INVALID_REQUEST";
    }
}
