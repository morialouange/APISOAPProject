package com.upg.legacyhr.soap;

import com.upg.legacyhr.soap.generated.AvailabilityStatus;
import com.upg.legacyhr.soap.generated.DepartmentInfo;
import com.upg.legacyhr.soap.generated.DepartmentList;
import com.upg.legacyhr.soap.generated.EmployeeList;
import com.upg.legacyhr.soap.generated.Proficiency;
import com.upg.legacyhr.soap.generated.Skill;
import com.upg.legacyhr.soap.generated.SkillList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Convertit les entites JPA du legacy en objets JAXB generes depuis le XSD.
 *
 * <p>Cette classe est le SEUL endroit ou le modele SQL est transforme en
 * modele XML. Elle est volontairement isolee de l'endpoint : l'endpoint ne
 * fait que de la logique de service, ce qui rend le contrat SOAP plus lisible
 * et les tests plus faciles.</p>
 *
 * <p>Point d'attention : la classe JAXB generee porte aussi le nom
 * {@code Employee}, d'ou l'usage du nom qualifie complet pour l'entite JPA.</p>
 */
@Component
public class EmployeeSoapMapper {

    /**
     * Transforme une entite JPA employe en objet JAXB {@code Employee}.
     *
     * <p>C'est ici que se construit la LISTE IMBRIQUEE : chaque employe
     * expose sa collection de competences.</p>
     */
    public com.upg.legacyhr.soap.generated.Employee toSoapEmployee(
            com.upg.legacyhr.model.Employee entity) {

        com.upg.legacyhr.soap.generated.Employee soapEmployee =
                new com.upg.legacyhr.soap.generated.Employee();

        soapEmployee.setId(entity.getId());
        soapEmployee.setEmployeeCode(entity.getEmployeeCode());
        soapEmployee.setFirstName(entity.getFirstName());
        soapEmployee.setLastName(entity.getLastName());
        soapEmployee.setFullName(entity.getFullName());
        soapEmployee.setDepartment(entity.getDepartment());
        soapEmployee.setAvailabilityStatus(toSoapStatus(entity.getStatus()));
        soapEmployee.setSkills(toSoapSkillList(entity));

        return soapEmployee;
    }

    /**
     * Construit la liste de competences imbriquee {@code <skills>}.
     *
     * <p>Le schema autorise {@code minOccurs="0"} : un employe sans
     * competence produit une liste vide, ce qui reste un document XML valide.</p>
     */
    private SkillList toSoapSkillList(com.upg.legacyhr.model.Employee entity) {
        SkillList skillList = new SkillList();

        if (entity.getSkills() == null || entity.getSkills().isEmpty()) {
            return skillList;
        }

        List<Skill> skills = entity.getSkills().stream()
                .map(skill -> {
                    Skill soapSkill = new Skill();
                    soapSkill.setSkillName(skill.getSkillName());
                    soapSkill.setProficiency(toSoapProficiency(skill.getProficiency()));
                    return soapSkill;
                })
                .toList();

        skillList.getSkill().addAll(skills);
        return skillList;
    }

    /**
     * Construit la liste d'employes {@code <employees>} de la reponse.
     */
    public EmployeeList toSoapEmployeeList(List<com.upg.legacyhr.model.Employee> entities) {
        EmployeeList employeeList = new EmployeeList();

        if (entities == null || entities.isEmpty()) {
            return employeeList;
        }

        employeeList.getEmployee()
                .addAll(entities.stream().map(this::toSoapEmployee).toList());

        return employeeList;
    }

    /**
     * Construit la liste des departements pour l'operation d'appui
     * {@code getAllDepartments}.
     *
     * @param lignes resultats de {@code COUNT(e) GROUP BY e.department}
     *               sous la forme {@code [departement, effectif]}
     */
    public DepartmentList toSoapDepartmentList(List<Object[]> lignes) {
        DepartmentList departmentList = new DepartmentList();

        for (Object[] ligne : lignes) {
            DepartmentInfo info = new DepartmentInfo();
            info.setCode((String) ligne[0]);
            info.setEmployeeCount(((Number) ligne[1]).intValue());
            departmentList.getDepartment().add(info);
        }

        return departmentList;
    }

    /** Conversion de l'enumeraison JPA vers le type simple du XSD. */
    private AvailabilityStatus toSoapStatus(com.upg.legacyhr.model.AvailabilityStatus statut) {
        if (statut == null) {
            return AvailabilityStatus.AVAILABLE;
        }
        return switch (statut) {
            case ON_LEAVE -> AvailabilityStatus.ON_LEAVE;
            case BUSY -> AvailabilityStatus.BUSY;
            case AVAILABLE -> AvailabilityStatus.AVAILABLE;
        };
    }

    /** Conversion de l'enumeration JPA vers le type simple du XSD. */
    private Proficiency toSoapProficiency(com.upg.legacyhr.model.Proficiency proficiency) {
        if (proficiency == null) {
            return Proficiency.BEGINNER;
        }
        return switch (proficiency) {
            case EXPERT -> Proficiency.EXPERT;
            case INTERMEDIATE -> Proficiency.INTERMEDIATE;
            case BEGINNER -> Proficiency.BEGINNER;
        };
    }
}
