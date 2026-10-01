package com.upg.legacyhr.model;

/**
 * Niveau de maitrise d'une competence.
 *
 * <p>Correspond a la colonne {@code employee_skill.proficiency} et au type
 * simple {@code hr:Proficiency} du contrat SOAP.</p>
 */
public enum Proficiency {

    /** Debutant. */
    BEGINNER,

    /** Niveau intermediaire. */
    INTERMEDIATE,

    /** Expert. */
    EXPERT
}
