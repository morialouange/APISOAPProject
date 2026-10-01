package com.upg.legacyhr.repository;

import com.upg.legacyhr.model.Employee;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acces aux donnees de la table {@code employee}.
 *
 * <p>Les requetes utilisent {@code JOIN FETCH} sur la collection
 * {@code skills} afin de charger la liste imbriquee en une seule requete SQL
 * (evite le probleme N+1 lors de la serialisation SOAP).</p>
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Retourne tous les employes d'un departement, avec leurs competences.
     *
     * @param departement code du departement, insensible a la casse (ex : "IT")
     * @return liste d'employes du departement, competencies comprise
     */
    @Query("""
            SELECT DISTINCT e
            FROM Employee e
            LEFT JOIN FETCH e.skills
            WHERE UPPER(e.department) = UPPER(:departement)
            ORDER BY e.id
            """)
    List<Employee> findByDepartmentWithSkills(@Param("departement") String departement);

    /**
     * Liste les departements existants avec leur effectif.
     * Aliment l'operation d'appui {@code getAllDepartments}.
     */
    @Query("""
            SELECT e.department, COUNT(e)
            FROM Employee e
            GROUP BY e.department
            ORDER BY e.department
            """)
    List<Object[]> countEmployeesByDepartment();

    /**
     * Verifie qu'un departement existe, sans charger les competences.
     */
    boolean existsByDepartmentIgnoreCase(String departement);
}
