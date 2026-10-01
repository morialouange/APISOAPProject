package com.upg.legacyhr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entite JPA {@code employee} du systeme RH legacy.
 *
 * <p>Modele SQL (annexe technique du sujet) :</p>
 * <pre>
 * employee(
 *   id            BIGINT PRIMARY KEY AUTO,
 *   employee_code VARCHAR UNIQUE,
 *   first_name    VARCHAR,
 *   last_name     VARCHAR,
 *   department    VARCHAR,
 *   status        VARCHAR (ACTIVE, ON_LEAVE)
 * )
 * </pre>
 *
 * <p>La relation avec {@code employee_skill} est declaree en {@code LAZY} et
 * resolue par un {@code JOIN FETCH} cote repository : c'est la liste imbriquee
 * que le SOAP doit renvoyer, on evite donc le probleme N+1.</p>
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AvailabilityStatus status;

    /**
     * Liste des competences de l'employe (table {@code employee_skill}).
     * Relation inverse : la cle etrangere {@code employee_id} est portee
     * par l'entite {@link EmployeeSkill}.
     */
    @OneToMany(mappedBy = "employee")
    private Set<EmployeeSkill> skills = new LinkedHashSet<>();

    public Employee() {
        // constructeur requis par JPA
    }

    /**
     * Nom complet concatene, expose dans la reponse SOAP sous le nom
     * {@code fullName}. Champ derive, non stocke en base.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }

    public Set<EmployeeSkill> getSkills() {
        return skills;
    }

    public void setSkills(Set<EmployeeSkill> skills) {
        this.skills = skills;
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", code=" + employeeCode
                + ", name=" + getFullName() + ", department=" + department
                + ", status=" + status + ", skills=" + skills.size() + "}";
    }
}
