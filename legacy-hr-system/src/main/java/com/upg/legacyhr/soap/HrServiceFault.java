package com.upg.legacyhr.soap;

/**
 * Classe de base des erreurs metier du service RH legacy.
 *
 * <p>Spring-WS 5 ne permet plus de muter une {@code SoapFault} apres
 * construction : le {@code faultcode} et le {@code faultstring} proviennent
 * donc de l'annotation {@code @SoapFault} portee par chaque sous-classe
 * (un code par type d'erreur), tandis que le message dynamique est depose
 * dans la balise {@code <detail>} par
 * {@code HrSoapFaultExceptionResolver}.</p>
 */
public abstract class HrServiceFault extends RuntimeException {

    protected HrServiceFault(String message) {
        super(message);
    }

    /**
     * Code applicatif, duplique ici pour permettre au code applicatif
     * (journalisation, tests) de le connaitre sans lire l'annotation.
     */
    public abstract String getFaultCode();

    /**
     * Element XML recevant le message dynamique dans la {@code <soap:Fault>}.
     *
     * <p>Le prefixe {@code hr} est declare automatiquement par Spring-WS sur
     * l'element {@code <detail>}.</p>
     */
    public javax.xml.namespace.QName getDetailName() {
        return new javax.xml.namespace.QName("http://upg.ac.rw/soap/hr", getFaultCode(), "hr");
    }
}
