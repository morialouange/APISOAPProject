


package com.upg.legacyhr.model;

/**
 * Disponibilite d'un employe.
 *
 * <p>Correspond a la colonne {@code employee.status} du systeme legacy.
 * Les valeurs sont alignees sur le type simple {@code hr:AvailabilityStatus}
 * du contrat SOAP afin que la conversion JPA -> XML soit directe.</p>
 *
 * <p>Note de mapping : l'annexe technique du cahier des charges mentionne
 * historiquement {@code (ACTIVE, ON_LEAVE)} pour cette colonne. Le contrat
 * SOAP expose desormais {@code AVAILABLE / ON_LEAVE / BUSY} afin de couvrir
 * les trois etats de disponibilite demandes en sortie.</p>
 */
public enum AvailabilityStatus {

    /** L'employe est disponible et peut recevoir une tache. */
    AVAILABLE,

    /** L'employe est en conge (equivaut a l'ancien "ACTIVE" inverse : ON_LEAVE). */
    ON_LEAVE,

    /** L'employe est surcharge / occupe sur un projet en cours. */
    BUSY
}
