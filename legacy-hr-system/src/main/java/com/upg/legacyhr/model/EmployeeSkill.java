package com.upg.legacyhr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entite JPA {@code employee_skill} du systeme RH legacy.
 *
 * <p>Modele SQL (annexe technique du sujet) :</p>
 * <pre>
 * employee_skill(
 *   id          BIGINT PRIMARY KEY AUTO,
 *   employee_id BIGINT FOREIGN KEY -> employee.id,
 *   skill_name  VARCHAR,
 *   proficiency VARCHAR
 * )
 * </pre>
 *
 * <p>Chaque ligne represente UNE competence d'UN employe. C'est cette table
 * qui produit la liste imbriquee {@code <skills>} de la reponse SOAP.</p>
 */
@Entity
@Table(name = "employee_skill")
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cle etrangere {@code employee_id} vers {@code employee.id}.
     * EAGER car la competence n'a aucun sens isolee de son employe.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false, length = 20)
    private Proficiency proficiency;

    public EmployeeSkill() {
        // constructeur requis par JPA
    }

    public EmployeeSkill(String skillName, Proficiency proficiency) {
        this.skillName = skillName;
        this.proficiency = proficiency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Proficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(Proficiency proficiency) {
        this.proficiency = proficiency;
    }

    @Override
    public String toString() {
        return "EmployeeSkill{skill=" + skillName + ", proficiency=" + proficiency + "}";
    }
}
